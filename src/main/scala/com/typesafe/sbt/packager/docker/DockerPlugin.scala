package com.typesafe.sbt.packager.docker

import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

import sbt._
import sbt.Keys.{clean, mappings, name, publish, publishLocal, sourceDirectory, streams, target, version}
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.{daemonUser, defaultLinuxInstallLocation}
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.stage
import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.Compat._
import com.typesafe.sbt.packager.{MappingsHelper, Stager}

import scala.sys.process.Process
import scala.util.Try

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
    val Docker: Configuration = config("docker")

    val DockerAlias = com.typesafe.sbt.packager.docker.DockerAlias
  }

  import autoImport._

  /**
    * The separator used by makeAdd should be always forced to UNIX separator.
    * The separator doesn't depend on the OS where Dockerfile is being built.
    */
  val UnixSeparatorChar = '/'

  override def requires: Plugins = UniversalPlugin

  override def projectConfigurations: Seq[Configuration] = Seq(Docker)

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    dockerBaseImage := "openjdk:latest",
    dockerExposedPorts := Seq(),
    dockerExposedUdpPorts := Seq(),
    dockerExposedVolumes := Seq(),
    dockerLabels := Map(),
    dockerEnvVars := Map(),
    dockerRepository := None,
    dockerUsername := None,
    dockerAlias := DockerAlias(
      (dockerRepository in Docker).value,
      (dockerUsername in Docker).value,
      (packageName in Docker).value,
      Some((version in Docker).value)
    ),
    dockerUpdateLatest := false,
    dockerEntrypoint := Seq(s"${(defaultLinuxInstallLocation in Docker).value}/bin/${executableScriptName.value}"),
    dockerCmd := Seq(),
    dockerExecCommand := Seq("docker"),
    dockerVersion := Try(Process(dockerExecCommand.value ++ Seq("version", "--format", "'{{.Server.Version}}'")).!!).toOption
      .map(_.trim)
      .flatMap(DockerVersion.parse),
    dockerBuildOptions := Seq("--force-rm") ++ Seq("-t", dockerAlias.value.versioned) ++ (
      if (dockerUpdateLatest.value)
        Seq("-t", dockerAlias.value.latest)
      else
        Seq()
    ),
    dockerRmiCommand := dockerExecCommand.value ++ Seq("rmi"),
    dockerBuildCommand := dockerExecCommand.value ++ Seq("build") ++ dockerBuildOptions.value ++ Seq("."),
    dockerCommands := {
      val dockerBaseDirectory = (defaultLinuxInstallLocation in Docker).value
      val user = (daemonUser in Docker).value
      val group = (daemonGroup in Docker).value

      val generalCommands = makeFrom(dockerBaseImage.value) +: makeMaintainer((maintainer in Docker).value).toSeq

      generalCommands ++
        Seq(makeWorkdir(dockerBaseDirectory)) ++ makeAdd(dockerVersion.value, dockerBaseDirectory, user, group) ++
        dockerLabels.value.map(makeLabel) ++
        dockerEnvVars.value.map(makeEnvVar) ++
        makeExposePorts(dockerExposedPorts.value, dockerExposedUdpPorts.value) ++
        makeVolumes(dockerExposedVolumes.value, user, group) ++
        Seq(makeUser(user), makeEntrypoint(dockerEntrypoint.value), makeCmd(dockerCmd.value))
    }
  ) ++ mapGenericFilesToDocker ++ inConfig(Docker)(
    Seq(
      executableScriptName := executableScriptName.value,
      mappings ++= dockerPackageMappings.value,
      name := name.value,
      packageName := packageName.value,
      publishLocal := {
        val log = streams.value.log
        publishLocalDocker(stage.value, dockerBuildCommand.value, log)
        log.info(s"Built image ${dockerAlias.value.versioned}")
      },
      publish := {
        val _ = publishLocal.value
        val alias = dockerAlias.value
        val log = streams.value.log
        val execCommand = dockerExecCommand.value
        publishDocker(execCommand, alias.versioned, log)
        if (dockerUpdateLatest.value) {
          publishDocker(execCommand, alias.latest, log)
        }
      },
      clean := {
        val alias = dockerAlias.value
        val log = streams.value.log
        val rmiCommand = dockerRmiCommand.value
        // clean up images
        rmiDocker(rmiCommand, alias.versioned, log)
        if (dockerUpdateLatest.value) {
          rmiDocker(rmiCommand, alias.latest, log)
        }
      },
      sourceDirectory := sourceDirectory.value / "docker",
      stage := Stager.stage(Docker.name)(streams.value, stagingDirectory.value, mappings.value),
      stage := (stage dependsOn dockerGenerateConfig).value,
      stagingDirectory := (target in Docker).value / "stage",
      target := target.value / "docker",
      daemonUser := "daemon",
      daemonGroup := daemonUser.value,
      defaultLinuxInstallLocation := "/opt/docker",
      dockerPackageMappings := MappingsHelper.contentOf(sourceDirectory.value),
      dockerGenerateConfig := generateDockerConfig(dockerCommands.value, stagingDirectory.value)
    )
  )

  /**
    * @param maintainer (optional)
    * @return LABEL MAINTAINER if defined
    */
  private final def makeMaintainer(maintainer: String): Option[CmdLike] =
    if (maintainer.isEmpty) None else Some(makeLabel(Tuple2("MAINTAINER", maintainer)))

  /**
    * @param dockerBaseImage
    * @return FROM command
    */
  private final def makeFrom(dockerBaseImage: String): CmdLike =
    Cmd("FROM", dockerBaseImage)

  /**
    * @param label
    * @return LABEL command
    */
  private final def makeLabel(label: (String, String)): CmdLike = {
    val (variable, value) = label
    Cmd("LABEL", variable + "=\"" + value.toString + "\"")
  }

  /**
    * @param envVar
    * @return ENV command
    */
  private final def makeEnvVar(envVar: (String, String)): CmdLike = {
    val (variable, value) = envVar
    Cmd("ENV", variable + "=\"" + value.toString + "\"")
  }

  /**
    * @param dockerBaseDirectory, the installation directory
    */
  private final def makeWorkdir(dockerBaseDirectory: String): CmdLike =
    Cmd("WORKDIR", dockerBaseDirectory)

  /**
    * @param dockerVersion
    * @param dockerBaseDirectory the installation directory
    * @param daemonUser
    * @param daemonGroup
    * @return ADD command adding all files inside the installation directory
    */
  private final def makeAdd(dockerVersion: Option[DockerVersion],
                            dockerBaseDirectory: String,
                            daemonUser: String,
                            daemonGroup: String): Seq[CmdLike] = {

    /**
      * This is the file path of the file in the Docker image, and does not depend on the OS where the image
      * is being built. This means that it needs to be the Unix file separator even when the image is built
      * on e.g. Windows systems.
      */
    val files = dockerBaseDirectory.split(UnixSeparatorChar)(1)

    if (dockerVersion.exists(DockerSupport.chownFlag)) {
      Seq(Cmd("ADD", s"--chown=$daemonUser:$daemonGroup $files /$files"))
    } else {
      Seq(Cmd("ADD", s"$files /$files"), makeChown(daemonUser, daemonGroup, "." :: Nil))
    }
  }

  /**
    * @param daemonUser
    * @param daemonGroup
    * @return chown command, owning the installation directory with the daemonuser
    */
  private final def makeChown(daemonUser: String, daemonGroup: String, directories: Seq[String]): CmdLike =
    ExecCmd("RUN", Seq("chown", "-R", s"$daemonUser:$daemonGroup") ++ directories: _*)

  /**
    * @param daemonUser
    * @return USER docker command
    */
  private final def makeUser(daemonUser: String): CmdLike =
    Cmd("USER", daemonUser)

  /**
    * @param entrypoint
    * @return ENTRYPOINT command
    */
  private final def makeEntrypoint(entrypoint: Seq[String]): CmdLike =
    ExecCmd("ENTRYPOINT", entrypoint: _*)

  /**
    * Default CMD implementation as default parameters to ENTRYPOINT.
    * @param args
    * @return CMD with args in exec form
    */
  private final def makeCmd(args: Seq[String]): CmdLike =
    ExecCmd("CMD", args: _*)

  /**
    * @param exposedPorts
    * @return if ports are exposed the EXPOSE command
    */
  private final def makeExposePorts(exposedPorts: Seq[Int], exposedUdpPorts: Seq[Int]): Option[CmdLike] =
    if (exposedPorts.isEmpty && exposedUdpPorts.isEmpty) None
    else
      Some(
        Cmd("EXPOSE", (exposedPorts.map(_.toString) ++ exposedUdpPorts.map(_.toString).map(_ + "/udp")) mkString " ")
      )

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
  private final def makeVolumes(exposedVolumes: Seq[String], daemonUser: String, daemonGroup: String): Seq[CmdLike] =
    if (exposedVolumes.isEmpty) Seq.empty
    else
      Seq(
        ExecCmd("RUN", Seq("mkdir", "-p") ++ exposedVolumes: _*),
        makeChown(daemonUser, daemonGroup, exposedVolumes),
        ExecCmd("VOLUME", exposedVolumes: _*)
      )

  /**
    * @param commands representing the Dockerfile
    * @return String representation of the Dockerfile described by commands
    */
  private final def makeDockerContent(commands: Seq[CmdLike]): String =
    Dockerfile(commands: _*).makeContent

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
    * uses the `mappings in Universal` to generate the
    * `mappings in Docker`.
    */
  def mapGenericFilesToDocker: Seq[Setting[_]] = {
    def renameDests(from: Seq[(File, String)], dest: String) =
      for {
        (f, path) <- from
        newPath = "%s/%s" format (dest, path)
      } yield (f, newPath)

    inConfig(Docker)(Seq(mappings := renameDests((mappings in Universal).value, defaultLinuxInstallLocation.value)))
  }

  private[docker] def publishLocalLogger(log: Logger) =
    new sys.process.ProcessLogger {
      override def err(err: => String): Unit =
        err match {
          case s if s.startsWith("Uploading context") =>
            log.debug(s) // pre-1.0
          case s if s.startsWith("Sending build context") =>
            log.debug(s) // 1.0
          case s if !s.trim.isEmpty => log.error(s)
          case s                    =>
        }

      override def out(inf: => String): Unit = inf match {
        case s if !s.trim.isEmpty => log.info(s)
        case s                    =>
      }

      override def buffer[T](f: => T): T = f
    }

  def publishLocalDocker(context: File, buildCommand: Seq[String], log: Logger): Unit = {
    log.debug("Executing Native " + buildCommand.mkString(" "))
    log.debug("Working directory " + context.toString)

    val ret = sys.process.Process(buildCommand, context) ! publishLocalLogger(log)

    if (ret != 0)
      throw new RuntimeException("Nonzero exit value: " + ret)
  }

  def rmiDocker(execCommand: Seq[String], tag: String, log: Logger): Unit = {
    def rmiDockerLogger(log: Logger) = new sys.process.ProcessLogger {
      override def err(err: => String): Unit = err match {
        case s if !s.trim.isEmpty => log.error(s)
        case s                    =>
      }

      override def out(inf: => String): Unit = log.info(inf)

      override def buffer[T](f: => T): T = f
    }

    log.debug(s"Removing ${tag}")

    val cmd = execCommand :+ tag
    val ret = sys.process.Process(cmd) ! rmiDockerLogger(log)

    if (ret != 0)
      sys.error(s"Nonzero exit value: ${ret}")
    else
      log.info(s"Removed image ${tag}")
  }

  def publishDocker(execCommand: Seq[String], tag: String, log: Logger): Unit = {
    val loginRequired = new AtomicBoolean(false)

    def publishLogger(log: Logger) =
      new sys.process.ProcessLogger {

        override def err(err: => String): Unit = err match {
          case s if !s.trim.isEmpty => log.error(s)
          case s                    =>
        }

        override def out(inf: => String): Unit =
          inf match {
            case s if s.startsWith("Please login") =>
              loginRequired.compareAndSet(false, true)
            case s if !loginRequired.get && !s.trim.isEmpty => log.info(s)
            case s                                          =>
          }

        override def buffer[T](f: => T): T = f
      }

    val cmd = execCommand ++ Seq("push", tag)

    log.debug("Executing " + cmd.mkString(" "))

    val ret = sys.process.Process(cmd) ! publishLogger(log)

    if (loginRequired.get)
      sys.error("""No credentials for repository, please run "docker login"""")
    else if (ret != 0)
      sys.error("Nonzero exit value: " + ret)
    else
      log.info("Published image " + tag)
  }

}
