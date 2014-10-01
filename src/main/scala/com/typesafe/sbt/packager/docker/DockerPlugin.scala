package com.typesafe.sbt
package packager
package docker
import Keys._
import universal._
import sbt._

trait DockerPlugin extends Plugin with UniversalPlugin {
  val Docker = config("docker") extend Universal

  def dockerSettings: Seq[Setting[_]] = Seq(
    dockerBaseImage := "dockerfile/java:latest",
    name in Docker <<= name,
    packageName in Docker <<= packageName,
    executableScriptName in Docker <<= executableScriptName,
    dockerRepository := None,
    dockerUpdateLatest := false,
    sourceDirectory in Docker <<= sourceDirectory apply (_ / "docker"),
    target in Docker <<= target apply (_ / "docker")

  ) ++ mapGenericFilesToDocker ++ inConfig(Docker)(Seq(
      daemonUser := "daemon",
      defaultLinuxInstallLocation := "/opt/docker",
      dockerExposedPorts := Seq(),
      dockerExposedVolumes := Seq(),
      dockerPackageMappings <<= (sourceDirectory) map { dir =>
        MappingsHelper contentOf dir
      },
      mappings <++= dockerPackageMappings,
      stage <<= (dockerGenerateContext, dockerGenerateConfig, streams) map { (_, dockerfile, s) =>
        s.log.success("created docker file: " + dockerfile.getPath)
      },
      dockerAddCommands := makeAddCommands(dockerGenerateContext.value),
      dockerGenerateContext <<= (cacheDirectory, mappings, target) map {
        (cacheDirectory, mappings, t) =>
          val contextDir = t / "files"
          stageFiles("docker")(cacheDirectory, contextDir, mappings)
          contextDir
      },
      // TODO this must be changed, when there is a setting for the startScripts name
      dockerGenerateConfig := {
        val dockerfile = target.value / "Dockerfile"

        val headerCommands = Seq(
          Cmd("FROM", dockerBaseImage.value),
          Cmd("MAINTAINER", maintainer.value)
        )
        val addCommands = dockerAddCommands.value
        val dockerCommands = Seq(
          Cmd("WORKDIR", "%s" format defaultLinuxInstallLocation.value),
          ExecCmd("RUN", "chown", "-R", daemonUser.value, "."),
          Cmd("USER", daemonUser.value),
          ExecCmd("ENTRYPOINT", "bin/%s" format executableScriptName.value),
          ExecCmd("CMD")
        )
        val exposeCommand = makeExposeCommands(dockerExposedPorts.value)
        val volumeCommands = makeVolumeCommands(dockerExposedVolumes.value)

        val content = Dockerfile(headerCommands ++ volumeCommands ++ exposeCommand ++ addCommands ++ dockerCommands: _*).makeContent

        IO.write(dockerfile, content)
        dockerfile
      },
      dockerTarget <<= (dockerRepository, packageName, version) map {
        (repo, name, version) =>
          repo.map(_ + "/").getOrElse("") + name + ":" + version
      },
      publishLocal <<= (dockerGenerateConfig, dockerGenerateContext, dockerTarget, dockerUpdateLatest, streams) map {
        (config, _, target, updateLatest, s) =>
          publishLocalDocker(config, target, updateLatest, s.log)
      },
      publish <<= (publishLocal, dockerTarget, dockerUpdateLatest, streams) map {
        (_, target, updateLatest, s) =>
          publishDocker(target, s.log)
          if (updateLatest) {
            val name = target.substring(0, target.lastIndexOf(":")) + ":latest"
            publishDocker(name, s.log)
          }
      }
    ))

  /**
   * After the files have been staged inside the (target in Docker) directory,
   * the add commands can be created by adding all files available inside the
   * "files" directory.
   */
  private[this] final def makeAddCommands(context: File): Seq[Cmd] = {
    val workingDir = context.getParentFile
    val mappings = MappingsHelper contentOf context
    mappings map {
      case (src, dest) => src.relativeTo(workingDir) -> dest
    } map {
      case (Some(src), dest) => Cmd("ADD", "/" + src.getPath + " " + dest)
      case (None, dest)      => sys.error("Could not create relative file for " + dest)
    }
  }

  private[this] final def makeExposeCommands(ports: Seq[Int]): Option[Cmd] = {
    if (ports isEmpty) None
    else Some(Cmd("EXPOSE", ports mkString " "))
  }

  /**
   * If the exposed volume does not exist, the volume is made available
   * with root ownership. This may be too strict for some directories,
   * and we lose the feature that all directories below the install path
   * can be written to by the binary. Therefore the directories are
   * created before the ownership is changed.
   */
  private[this] final def makeVolumeCommands(volumes: Seq[String]): Seq[ExecCmd] = {
    if (volumes isEmpty) Seq()
    else Seq(
      ExecCmd("RUN", Seq("mkdir", "-p") ++ volumes: _*),
      ExecCmd("VOLUME", volumes: _*)
    )
  }

  /**
   * maps the docker:mappings to defaultLinuxInstallation/mapping-path
   */
  def mapGenericFilesToDocker: Seq[Setting[_]] = {
    def renameDests(from: Seq[(File, String)], dest: String) = {
      for {
        (f, path) <- from
        newPath = "%s/%s" format (dest, path)
      } yield (f, newPath)
    }

    // create the settings seq
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
    val cwd = context.getParentFile

    log.debug("Executing " + cmd.mkString(" "))
    log.debug("Working directory " + cwd.toString)

    val ret = Process(cmd, cwd) ! publishLocalLogger(log)

    if (ret != 0)
      throw new RuntimeException("Nonzero exit value: " + ret)
    else
      log.info("Built image " + tag)

    if (latest) {
      val name = tag.substring(0, tag.lastIndexOf(":")) + ":latest"
      val latestCmd = Seq("docker", "tag", tag, name)
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
