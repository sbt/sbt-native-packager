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
import SbtNativePackager.Universal

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
    val Docker = config("docker") extend Universal
  }

  import autoImport._

  override def requires = universal.UniversalPlugin

  override lazy val projectSettings = Seq(
    dockerBaseImage := "dockerfile/java:latest",
    dockerExposedPorts := Seq(),
    dockerExposedVolumes := Seq(),
    dockerRepository := None,
    dockerUpdateLatest := false,
    dockerEntrypoint := Seq("bin/%s" format executableScriptName.value)

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
      defaultLinuxInstallLocation := "/opt/docker",

      dockerPackageMappings <<= sourceDirectory map { dir =>
        MappingsHelper contentOf dir
      },
      dockerGenerateConfig <<= (dockerBaseImage, defaultLinuxInstallLocation,
        maintainer, daemonUser, executableScriptName,
        dockerExposedPorts, dockerExposedVolumes, target, dockerEntrypoint) map generateDockerConfig,
      dockerTarget <<= (dockerRepository, packageName, version) map {
        (repo, name, version) =>
          repo.map(_ + "/").getOrElse("") + name + ":" + version
      }
    ))

  private[this] final def makeDockerContent(dockerBaseImage: String, dockerBaseDirectory: String, maintainer: String, daemonUser: String, execScript: String, exposedPorts: Seq[Int], exposedVolumes: Seq[String], entrypoint: Seq[String]) = {
    val fromCommand = Cmd("FROM", dockerBaseImage)

    val maintainerCommand: Option[Cmd] = {
      if (maintainer.isEmpty)
        None
      else
        Some(Cmd("MAINTAINER", maintainer))
    }

    val files = dockerBaseDirectory.split(java.io.File.separator)(1)

    val dockerCommands = Seq(
      Cmd("ADD", s"$files /$files"),
      Cmd("WORKDIR", "%s" format dockerBaseDirectory),
      ExecCmd("RUN", "chown", "-R", daemonUser, "."),
      Cmd("USER", daemonUser),
      ExecCmd("ENTRYPOINT", entrypoint: _*),
      ExecCmd("CMD")
    )

    val exposeCommand: Option[CmdLike] = {
      if (exposedPorts.isEmpty)
        None
      else
        Some(Cmd("EXPOSE", exposedPorts.mkString(" ")))
    }

    // If the exposed volume does not exist, the volume is made available
    // with root ownership. This may be too strict for some directories,
    // and we lose the feature that all directories below the install path
    // can be written to by the binary. Therefore the directories are
    // created before the ownership is changed.
    val volumeCommands: Seq[CmdLike] = {
      if (exposedVolumes.isEmpty)
        Seq()
      else
        Seq(
          ExecCmd("RUN", Seq("mkdir", "-p") ++ exposedVolumes: _*),
          ExecCmd("VOLUME", exposedVolumes: _*)
        )
    }

    val commands =
      Seq(fromCommand) ++ maintainerCommand ++ volumeCommands ++ exposeCommand ++ dockerCommands

    Dockerfile(commands: _*).makeContent
  }

  private[this] final def generateDockerConfig(
    dockerBaseImage: String, dockerBaseDirectory: String, maintainer: String, daemonUser: String, execScript: String, exposedPorts: Seq[Int], exposedVolumes: Seq[String], target: File, entrypoint: Seq[String]
  ) = {
    val dockerContent = makeDockerContent(dockerBaseImage, dockerBaseDirectory, maintainer, daemonUser, execScript, exposedPorts, exposedVolumes, entrypoint)

    val f = target / "Dockerfile"
    IO.write(f, dockerContent)
    f
  }

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

    log.debug("Executing " + cmd.mkString(" "))
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
        case 0 => log.info("Update Latest from image" + tag)
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
