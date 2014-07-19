package com.typesafe.sbt
package packager
package docker
import Keys._
import universal._
import sbt._

trait DockerPlugin extends Plugin with UniversalPlugin {
  val Docker = config("docker") extend Universal

  private[this] final def makeDockerContent(dockerBaseImage: String, dockerBaseDirectory: String, maintainer: String, daemonUser: String, name: String, exposedPorts: Seq[Int], exposedVolumes: Seq[String]) = {
    val headerCommands = Seq(
      Cmd("FROM", dockerBaseImage),
      Cmd("MAINTAINER", maintainer)
    )

    val dockerCommands = Seq(
      Cmd("ADD", "files /"),
      Cmd("WORKDIR", "%s" format dockerBaseDirectory),
      ExecCmd("RUN", "chown", "-R", daemonUser, "."),
      Cmd("USER", daemonUser),
      ExecCmd("ENTRYPOINT", "bin/%s" format name),
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

    Dockerfile(headerCommands ++ volumeCommands ++ exposeCommand ++ dockerCommands: _*).makeContent
  }

  private[this] final def generateDockerConfig(
    dockerBaseImage: String, dockerBaseDirectory: String, maintainer: String, daemonUser: String, packageName: String, exposedPorts: Seq[Int], exposedVolumes: Seq[String], target: File) = {
    val dockerContent = makeDockerContent(dockerBaseImage, dockerBaseDirectory, maintainer, daemonUser, packageName, exposedPorts, exposedVolumes)

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

  def publishLocalDocker(context: File, tag: String, log: Logger): Unit = {
    val cmd = Seq("docker", "build", "--force-rm", "-t", tag, ".")
    val cwd = context.getParentFile

    log.debug("Executing " + cmd.mkString(" "))
    log.debug("Working directory " + cwd.toString)

    val ret = Process(cmd, cwd) ! publishLocalLogger(log)

    if (ret != 0)
      throw new RuntimeException("Nonzero exit value: " + ret)
    else
      log.info("Built image " + tag)
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

  def dockerSettings: Seq[Setting[_]] = Seq(
    dockerBaseImage := "dockerfile/java",
    name in Docker <<= name,
    packageName in Docker <<= packageName,
    dockerRepository := None,
    sourceDirectory in Docker <<= sourceDirectory apply (_ / "docker"),
    target in Docker <<= target apply (_ / "docker"),

    // TODO this must be changed, when there is a setting for the startScripts name
    dockerGenerateConfig <<=
      (dockerBaseImage in Docker, defaultLinuxInstallLocation in Docker, maintainer in Docker, daemonUser in Docker,
        packageName /* this is not scoped!*/ , dockerExposedPorts in Docker, dockerExposedVolumes in Docker, target in Docker) map
        generateDockerConfig
  ) ++ mapGenericFilesToDocker ++ inConfig(Docker)(Seq(
      daemonUser := "daemon",
      defaultLinuxInstallLocation := "/opt/docker",
      dockerExposedPorts := Seq(),
      dockerExposedVolumes := Seq(),
      dockerPackageMappings <<= (sourceDirectory) map { dir =>
        MappingsHelper contentOf dir
      },
      mappings <++= dockerPackageMappings,
      stage <<= (dockerGenerateConfig, dockerGenerateContext) map { (configFile, contextDir) => () },
      dockerGenerateContext <<= (cacheDirectory, mappings, target) map {
        (cacheDirectory, mappings, t) =>
          val contextDir = t / "files"
          stageFiles("docker")(cacheDirectory, contextDir, mappings)
          contextDir
      },
      dockerTarget <<= (dockerRepository, packageName, version) map {
        (repo, name, version) =>
          repo.map(_ + "/").getOrElse("") + name + ":" + version
      },
      publishLocal <<= (dockerGenerateConfig, dockerGenerateContext, dockerTarget, streams) map {
        (config, _, target, s) =>
          publishLocalDocker(config, target, s.log)
      },
      publish <<= (publishLocal, dockerTarget, streams) map {
        (_, target, s) =>
          publishDocker(target, s.log)
      }
    ))
}
