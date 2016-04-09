package com.typesafe.sbt
package packager
package docker

import sbt._
import sbt.Keys.{
  name,
  version,
  target,
  mappings,
  publish,
  publishLocal,
  publishArtifact,
  sourceDirectory,
  streams,
  cacheDirectory
}
import packager.Keys._
import linux.LinuxPlugin.autoImport.{ daemonUser, defaultLinuxInstallLocation }
import universal.UniversalPlugin.autoImport.stage
import SbtNativePackager.{ Universal, Linux }

/**
 * == Docker Plugin ==
 *
 * This plugin helps you build docker containers.
 *
 * == Configuration ==
 *
 * In order to configure this plugin take a look at the available [[com.typesafe.sbt.packager.docker.DockerKeys]]
 *
 * == Requirements ==
 *
 * You need docker to have docker installed on your system and be able to execute commands.
 * Check with a single command:
 *
 * {{{
 * docker version
 * }}}
 *
 * Future versions of the Docker Plugin may use the REST API, so you don't need docker installed
 * locally.
 *
 * @note this plugin is not intended to build very customizable docker images, but turn your mappings
 * configuration in a docker image with almost no ''any'' configuration.
 *
 * @example Enable the plugin in the `build.sbt`
 * {{{
 *    enablePlugins(DockerPlugin)
 * }}}
 */
object DockerPlugin extends AutoPlugin {

  object autoImport extends DockerKeys {
    val Docker = config("docker")
  }

  import autoImport._

  /**
   * The separator for makeAdd force UNIX separator.
   * The separator doesn't depend to OS where i build Dockerfile.
   */
  val UnixSeparatorChar = '/'

  override def requires = universal.UniversalPlugin

  override def projectConfigurations: Seq[Configuration] =  Seq(Docker)

  override lazy val projectSettings = Seq(
    dockerBaseImage := "java:latest",
    dockerExposedPorts := Seq(),
    dockerExposedVolumes := Seq(),
    dockerRepository := None,
    dockerUpdateLatest := false,
    dockerEntrypoint := Seq("bin/%s" format executableScriptName.value),
    dockerCmd := Seq(),
    dockerCommands := {
      val dockerBaseDirectory = (defaultLinuxInstallLocation in Docker).value
      val user = (daemonUser in Docker).value
      val group = (daemonGroup in Docker).value

      val generalCommands = makeFrom(dockerBaseImage.value) +: makeMaintainer((maintainer in Docker).value).toSeq

      generalCommands ++ Seq(
        makeWorkdir(dockerBaseDirectory),
        makeAdd(dockerBaseDirectory),
        makeChown(user, group, "." :: Nil)
      ) ++
        makeExposePorts(dockerExposedPorts.value) ++
        makeVolumes(dockerExposedVolumes.value, user, group) ++
        Seq(
          makeUser(user),
          makeEntrypoint(dockerEntrypoint.value),
          makeCmd(dockerCmd.value)
        )

    }

  ) ++ mapGenericFilesToDocker ++ inConfig(Docker)(Seq(
      executableScriptName := executableScriptName.value,
      mappings ++= dockerPackageMappings.value,
      mappings ++= Seq(dockerGenerateConfig.value) pair relativeTo(target.value),
      name := name.value,
      packageName := packageName.value,
      publishLocal <<= (stage, dockerTarget, dockerUpdateLatest, streams) map {
        (context, target, updateLatest, s) =>
          publishLocalDocker(context, target, updateLatest, s.log)
      },
      publish <<= (publishLocal, dockerTarget, dockerUpdateLatest, streams) map {
        (_, target, updateLatest, s) =>
          publishDocker(target, s.log)
          if (updateLatest) {
            val name = target.substring(0, target.lastIndexOf(":")) + ":latest"
            publishDocker(name, s.log)
          }
      },
      sourceDirectory := sourceDirectory.value / "docker",
      stage <<= (streams, stagingDirectory, mappings) map Stager.stage(Docker.name),
      stagingDirectory := (target in Docker).value / "stage",
      target := target.value / "docker",

      daemonUser := "daemon",
      daemonGroup := daemonUser.value,
      defaultLinuxInstallLocation := "/opt/docker",

      dockerPackageMappings <<= sourceDirectory map { dir =>
        MappingsHelper contentOf dir
      },
      dockerGenerateConfig <<= (dockerCommands, target) map generateDockerConfig,
      dockerTarget <<= (dockerRepository, packageName, version) map {
        (repo, name, version) => repo.map(_ + "/").getOrElse("") + name + ":" + version
      }
    ))

  /**
   * @param maintainer (optional)
   * @return MAINTAINER if defined
   */
  private final def makeMaintainer(maintainer: String): Option[CmdLike] =
    if (maintainer.isEmpty) None else Some(Cmd("MAINTAINER", maintainer))

  /**
   * @param dockerBaseImage
   * @return FROM command
   */
  private final def makeFrom(dockerBaseImage: String): CmdLike = Cmd("FROM", dockerBaseImage)

  /**
   * @param dockerBaseDirectory, the installation directory
   * @param WORKDIR command, setting dockerBaseDirectory as cwd
   */
  private final def makeWorkdir(dockerBaseDirectory: String): CmdLike = Cmd("WORKDIR", dockerBaseDirectory)

  /**
   * @param dockerBaseDirectory, the installation directory
   * @return ADD command adding all files inside the installation directory
   */
  private final def makeAdd(dockerBaseDirectory: String): CmdLike = {
    val files = dockerBaseDirectory.split(UnixSeparatorChar)(1)
    Cmd("ADD", s"$files /$files")
  }

  /**
   * @param daemonUser
   * @param daemonGroup
   * @param directory to chown recursively
   * @return chown command, owning the installation directory with the daemonuser
   */
  private final def makeChown(daemonUser: String, daemonGroup: String, directories: Seq[String]): CmdLike =
    ExecCmd("RUN", Seq("chown", "-R", s"$daemonUser:$daemonGroup") ++ directories: _*)

  /**
   * @param daemonUser
   * @return USER docker command
   */
  private final def makeUser(daemonUser: String): CmdLike = Cmd("USER", daemonUser)

  /**
   * @param entrypoint
   * @return ENTRYPOINT command
   */
  private final def makeEntrypoint(entrypoint: Seq[String]): CmdLike = ExecCmd("ENTRYPOINT", entrypoint: _*)

  /**
   * Default CMD implementation as default parameters to ENTRYPOINT.
   * @param args
   * @return CMD with args in exec form
   */
  private final def makeCmd(args: Seq[String]): CmdLike = ExecCmd("CMD", args: _*)

  /**
   * @param exposedPorts
   * @return if ports are exposed the EXPOSE command
   */
  private final def makeExposePorts(exposedPorts: Seq[Int]): Option[CmdLike] = {
    if (exposedPorts.isEmpty) None else Some(Cmd("EXPOSE", exposedPorts mkString " "))
  }

  /**
   * If the exposed volume does not exist, the volume is made available
   * with root ownership. This may be too strict for some directories,
   * and we lose the feature that all directories below the install path
   * can be written to by the binary. Therefore the directories are
   * created before the ownership is changed.
   *
   * All directories created afterwards are chowned.
   *
   * @param exposedVolumes
   * @return commands to create, chown and declare volumes
   * @see http://stackoverflow.com/questions/23544282/what-is-the-best-way-to-manage-permissions-for-docker-shared-volumes
   * @see https://docs.docker.com/userguide/dockervolumes/
   */
  private final def makeVolumes(exposedVolumes: Seq[String], daemonUser: String, daemonGroup: String): Seq[CmdLike] = {
    if (exposedVolumes.isEmpty) Seq.empty
    else Seq(
      ExecCmd("RUN", Seq("mkdir", "-p") ++ exposedVolumes: _*),
      makeChown(daemonUser, daemonGroup, exposedVolumes),
      ExecCmd("VOLUME", exposedVolumes: _*)
    )
  }

  /**
   * @param commands representing the Dockerfile
   * @return String representation of the Dockerfile described by commands
   */
  private final def makeDockerContent(commands: Seq[CmdLike]): String = Dockerfile(commands: _*).makeContent

  /**
   * @param commands, docker content
   * @param target directory for Dockerfile
   * @return Dockerfile
   */
  private[this] final def generateDockerConfig(commands: Seq[CmdLike], target: File): File = {
    val dockerContent = makeDockerContent(commands)

    val f = target / "Dockerfile"
    IO.write(f, dockerContent)
    f
  }

  /**
   * uses the `mappings in Unversial` to generate the
   * `mappings in Docker`.
   */
  def mapGenericFilesToDocker: Seq[Setting[_]] = {
    def renameDests(from: Seq[(File, String)], dest: String) = {
      for {
        (f, path) <- from
        newPath = "%s/%s" format (dest, path)
      } yield (f, newPath)
    }

    inConfig(Docker)(Seq(
      mappings <<= (mappings in Universal, defaultLinuxInstallLocation) map { (mappings, dest) =>
        renameDests(mappings, dest)
      }
    ))
  }

  private[docker] def publishLocalLogger(log: Logger) = {
    new ProcessLogger {
      def error(err: => String) = {
        err match {
          case s if s.startsWith("Uploading context") => log.debug(s) // pre-1.0
          case s if s.startsWith("Sending build context") => log.debug(s) // 1.0
          case s if !s.trim.isEmpty => log.error(s)
          case s =>
        }
      }

      def info(inf: => String) = inf match {
        case s if !s.trim.isEmpty => log.info(s)
        case s                    =>
      }

      def buffer[T](f: => T) = f
    }
  }

  def publishLocalDocker(context: File, tag: String, latest: Boolean, log: Logger): Unit = {
    val cmd = Seq("docker", "build", "--force-rm", "-t", tag, ".")

    log.debug("Executing Native " + cmd.mkString(" "))
    log.debug("Working directory " + context.toString)

    val ret = Process(cmd, context) ! publishLocalLogger(log)

    if (ret != 0)
      throw new RuntimeException("Nonzero exit value: " + ret)
    else
      log.info("Built image " + tag)

    if (latest) {
      val name = tag.substring(0, tag.lastIndexOf(":")) + ":latest"
      val latestCmd = Seq("docker", "tag", "-f", tag, name)
      Process(latestCmd).! match {
        case 0 => log.info("Update Latest from image " + tag)
        case n => sys.error("Failed to run docker tag")
      }
    }
  }

  def publishDocker(tag: String, log: Logger): Unit = {
    @volatile
    var loginRequired = false

    def publishLogger(log: Logger) = {
      new ProcessLogger {

        def error(err: => String) = err match {
          case s if !s.trim.isEmpty => log.error(s)
          case s                    =>
        }

        def info(inf: => String) = {
          inf match {
            case s if !loginRequired && s.startsWith("Please login") =>
              loginRequired = true
            case s if !loginRequired && !s.trim.isEmpty => log.info(s)
            case s                                      =>
          }
        }

        def buffer[T](f: => T) = f
      }
    }

    val cmd = Seq("docker", "push", tag)

    log.debug("Executing " + cmd.mkString(" "))

    val ret = Process(cmd) ! publishLogger(log)

    if (loginRequired)
      throw new RuntimeException("""No credentials for repository, please run "docker login"""")
    else if (ret != 0)
      throw new RuntimeException("Nonzero exit value: " + ret)
    else
      log.info("Published image " + tag)
  }

}
