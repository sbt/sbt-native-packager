package com.typesafe.sbt
package packager
package docker
import Keys._
import universal._
import sbt._
import sbt.Keys.{ organization }

trait DockerPlugin extends Plugin with UniversalPlugin {
  val Docker = config("docker") extend Universal

  import DockerPlugin._

  def dockerSettings: Seq[Setting[_]] = Seq(
    dockerBaseImage := "dockerfile/java:latest",
    name in Docker <<= name,
    packageName in Docker <<= packageName,
    executableScriptName in Docker <<= executableScriptName,
    dockerRepository := None,
    dockerUpdateLatest := false,
    dockerAppLibraryRegex := s"lib/${(organization in Docker).value}.*\\.jar$$",
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
      dockerAddCommands := makeAddCommands(dockerGenerateContext.value, defaultLinuxInstallLocation.value),
      dockerGenerateContext <<= (streams, mappings, target) map {
        (s, mappings, t) =>
          val contextDir = t / "files"
          stageFiles("docker")(s.cacheDirectory, contextDir, mappings)
          contextDir
      },
      dockerGenerateConfig := {
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

        val cacheDirectory = streams.value.cacheDirectory / "Dockerfile"
        val cacheDockerFile = cacheDirectory / "Dockerfile"
        IO.write(cacheDockerFile, content)

        val cachedDockerfile = FileFunction.cached(cacheDirectory, inStyle = FilesInfo.hash, outStyle = FilesInfo.exists) {
          (in: Set[File]) =>
            val dockerfile = target.value / "Dockerfile"
            val cache = in.headOption getOrElse (sys.error("Couldn't find Dockerfile in cache"))
            IO.copyFile(cache, dockerfile, true)
            Set(dockerfile)
        }

        cachedDockerfile(Set(cacheDockerFile)).headOption getOrElse (sys.error("Couldn't find Dockerfile in cache"))
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
   *
   * This dependes on the _mapGenericFilesToDocker_ method, which splits up
   * the mappings into app and lib folders, so docker can cache these jars.
   * The add commands will map both app and lib folder again to the single
   * destination lib folder.
   *
   * @param context - the docker build context (should be: target in Docker)
   * @param installLocation - inside the docker container (should be: defaultLinuxInstallation in Docker)
   *
   */
  private[this] final def makeAddCommands(context: File, installLocation: String): Seq[Cmd] = {
    val directories = (context / installLocation).list
    directories map {
      case APP_DIR => Cmd("ADD", s"/files$installLocation/$APP_DIR $installLocation/$LIB_DIR")
      case folder  => Cmd("ADD", s"/files$installLocation/$folder $installLocation/$folder")
    }
  }

  /**
   * @param ports - a list for ports to expose
   */
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
   *
   * @param volumes - list of volumes to add
   */
  private[this] final def makeVolumeCommands(volumes: Seq[String]): Seq[ExecCmd] = {
    if (volumes isEmpty) Seq()
    else Seq(
      ExecCmd("RUN", Seq("mkdir", "-p") ++ volumes: _*),
      ExecCmd("VOLUME", volumes: _*)
    )
  }

  /**
   * Maps the docker:mappings to defaultLinuxInstallation/mapping-path.
   *
   * It will split up the library dependencies into an app and lib folder
   * to allow docker to cache the jars.
   *
   * @return new mapping settings in Docker scope
   */
  def mapGenericFilesToDocker: Seq[Setting[_]] = inConfig(Docker)(Seq(
    mappings := {
      val log = streams.value.log
      val installLocation = (defaultLinuxInstallLocation in Docker).value
      val r = (dockerAppLibraryRegex in Docker).value.r

      def isApp(path: String): Boolean = r.findFirstIn(path).isDefined

      // this is only for user information
      val (apps, other) = (mappings in Universal).value partition (m => isApp(m._2))
      log.info(s"${apps.size} mappings in app/")

      // the actual mapping from app jars to the app folder
      (mappings in Universal).value map {
        case (f, path) if isApp(path) => f -> s"$installLocation/${path.replaceFirst(LIB_DIR, APP_DIR)}"
        case (f, path)                => f -> s"$installLocation/$path"
      }
    }
  ))

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

    Process(cmd, cwd) ! publishLocalLogger(log) match {
      case 0 => log.info("Built image " + tag)
      case n => throw new RuntimeException("Nonzero exit value: " + n)
    }

    if (latest) {
      val name = tag.substring(0, tag.lastIndexOf(":")) + ":latest"
      val latestCmd = Seq("docker", "tag", tag, name)
      Process(latestCmd) ! log match {
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

object DockerPlugin {
  private[docker] val LIB_DIR = "lib"
  private[docker] val APP_DIR = "app"

}
