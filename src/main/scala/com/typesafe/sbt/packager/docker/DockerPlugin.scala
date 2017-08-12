package com.typesafe.sbt
package packager
package docker

import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import sbt._
import sbt.Keys._
import packager.Keys._
import linux.LinuxPlugin.autoImport.{daemonUser, defaultLinuxInstallLocation}
import universal.UniversalPlugin.autoImport.stage
import SbtNativePackager.{Linux, Universal}

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
  *       configuration in a docker image with almost no ''any'' configuration.
  * @example Enable the plugin in the `build.sbt`
  *          {{{
  *                        enablePlugins(DockerPlugin)
  *          }}}
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

    override def requires = universal.UniversalPlugin

    override def projectConfigurations: Seq[Configuration] = Seq(Docker)

    override lazy val projectSettings: Seq[Setting[_]] = Seq(
        dockerBaseImage := "openjdk:latest",
        dockerExposedPorts := Seq(),
        dockerExposedUdpPorts := Seq(),
        dockerExposedVolumes := Seq(),
        dockerLabels := Map(),
        dockerRepository := None,
        dockerUsername := None,
        dockerAlias := DockerAlias(
            dockerRepository.value,
            dockerUsername.value,
            (packageName in Docker).value,
            Some((version in Docker).value)
        ),
        dockerUpdateLatest := false,
        dockerEntrypoint := Seq("bin/%s" format executableScriptName.value),
        dockerCmd := Seq(),
        dockerExecCommand := Seq("docker"),
        dockerBuildOptions := Seq("--force-rm", "--no-cache", "--pull") ++ Seq("-t", dockerAlias.value.versioned) ++ (
          if (dockerUpdateLatest.value)
              Seq("-t", dockerAlias.value.latest)
          else
              Seq()
          ),
        dockerRmiCommand := dockerExecCommand.value ++ Seq("rmi"),
        dockerBuildCommand := dockerExecCommand.value ++ Seq("build") ++ dockerBuildOptions.value ++ Seq("."),
        dockerCommands := {
            val dockerBaseDirectory: String = (defaultLinuxInstallLocation in Docker).value
            val user = (daemonUser in Docker).value
            val group = (daemonGroup in Docker).value

            val generalCommands = makeFrom(dockerBaseImage.value) +: makeMaintainer((maintainer in Docker).value).toSeq

            generalCommands ++
              Seq(makeWorkdir(dockerBaseDirectory), makeAdd(dockerBaseDirectory), makeChown(user, group, "." :: Nil)) ++
              dockerLabels.value.map(makeLabel) ++
              dockerDocfileCommands.value ++
              makeExposePorts(dockerExposedPorts.value, dockerExposedUdpPorts.value) ++
              makeVolumes(dockerExposedVolumes.value, user, group) ++
              Seq(makeUser(user), makeEntrypoint(dockerEntrypoint.value), makeCmd(dockerCmd.value)).filter(_ != EmptyCmd)
        }
    ) ++ mapGenericFilesToDocker ++ inConfig(Docker)(
        Seq(
            executableScriptName := executableScriptName.value,
            mappings ++= dockerPackageMappings.value,
            mappings ++= Seq(dockerGenerateConfig.value) pair relativeTo(target.value),
            name := name.value,
            packageName := packageName.value,
            publishLocal := {
                val log = streams.value.log
                publishLocalDocker(target.value, dockerBuildCommand.value, log)
                log.info(s"Built image ${dockerAlias.value.versioned}")
            },
            publish := {
                val _ = publishLocal.value
                val alias = dockerAlias.value
                val log = streams.value.log
                publishDocker(dockerExecCommand.value, alias.versioned, log)
                if (dockerUpdateLatest.value) {
                    publishDocker(dockerExecCommand.value, alias.latest, log)
                }
            },
            clean := {
                val alias = dockerAlias.value
                val log = streams.value.log
                rmiDocker(dockerRmiCommand.value, alias.versioned, log)
                if (dockerUpdateLatest.value) {
                    rmiDocker(dockerRmiCommand.value, alias.latest, log)
                }
            },
            sourceDirectory := sourceDirectory.value / "docker",
            stage := Stager.stage(Docker.name)(streams.value, stagingDirectory.value, mappings.value),
            stagingDirectory := (target in Docker).value / "stage",
            target := target.value,
            daemonUser := "daemon",
            daemonGroup := daemonUser.value,
            defaultLinuxInstallLocation := "/opt/docker",
            dockerPackageMappings := MappingsHelper.contentOf(sourceDirectory.value),
            dockerGenerateConfig := generateDockerConfig(dockerCommands.value, target.value)
        )
    )

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
    private final def makeFrom(dockerBaseImage: String): CmdLike =
        Cmd("FROM", dockerBaseImage)

    /**
      * @param label
      * @return LABEL command
      */
    private final def makeLabel(label: Tuple2[String, String]): CmdLike = {
        val (variable, value) = label
        Cmd("LABEL", s"${variable}=${value}")
    }

    /**
      * @param dockerBaseDirectory , the installation directory
      */
    private final def makeWorkdir(dockerBaseDirectory: String): CmdLike = if (dockerBaseDirectory.nonEmpty)
        Cmd("WORKDIR", dockerBaseDirectory) else EmptyCmd

    /**
      * @param dockerBaseDirectory , the installation directory
      * @return ADD command adding all files inside the installation directory
      */
    private final def makeAdd(dockerBaseDirectory: String): CmdLike = {

        /**
          * This is the file path of the file in the Docker image, and does not depend on the OS where the image
          * is being built. This means that it needs to be the Unix file separator even when the image is built
          * on e.g. Windows systems.
          */
        dockerBaseDirectory.split(UnixSeparatorChar).headOption.find(_.nonEmpty).map(files ⇒ Cmd("ADD", s"$files /$files")).getOrElse(EmptyCmd)
    }

    /**
      * @param daemonUser
      * @param daemonGroup
      * @return chown command, owning the installation directory with the daemonuser
      */
    private final def makeChown(daemonUser: String, daemonGroup: String, directories: Seq[String]): CmdLike = if (daemonGroup.isEmpty && daemonUser.isEmpty) EmptyCmd else
        ExecCmd("RUN", Seq("chown", "-R", s"$daemonUser:$daemonGroup") ++ directories: _*)

    /**
      * @param daemonUser
      * @return USER docker command
      */
    private final def makeUser(daemonUser: String): CmdLike = if (daemonUser.nonEmpty)
        Cmd("USER", daemonUser) else EmptyCmd

    /**
      * @param entrypoint
      * @return ENTRYPOINT command
      */
    private final def makeEntrypoint(entrypoint: Seq[String]): CmdLike = if (entrypoint.nonEmpty)
        ExecCmd("ENTRYPOINT", entrypoint: _*) else EmptyCmd

    /**
      * Default CMD implementation as default parameters to ENTRYPOINT.
      *
      * @param args
      * @return CMD with args in exec form
      */
    private final def makeCmd(args: Seq[String]): CmdLike = if (args.nonEmpty)
        ExecCmd("CMD", args: _*) else EmptyCmd

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
      * @param commands , docker content
      * @param target   directory for Dockerfile
      * @return Dockerfile
      */
    private[this] final def generateDockerConfig(commands: Seq[CmdLike], target: File): File = {
        val dockerContent = makeDockerContent(commands)

        val f = target / "Dockerfile"
        IO.write(f, dockerContent)
        f
    }

    /**
      * @param args arguments
      * @return ExceCmd
      */
    final def RUN(args: String*): CmdLike = Cmd("RUN", args: _*)

    /**
      * @param args arguments
      * @return ExceCmd
      */
    final def RUN$(args: String*): CmdLike = ExecCmd("RUN", (args.init ++ Seq("/bin/bash", "-c") ++ Seq(args.last)): _*)

    /**
      * @param args arguments
      * @return ExceCmd
      */
    final def ENV(args: String*): CmdLike = Cmd("ENV", args: _*)

    /**
      * @param args arguments
      * @return ExceCmd
      */
    final def COPY(args: String*): CmdLike = Cmd("COPY", args: _*)

    /**
      * @param args arguments
      * @return ExceCmd
      */
    final def CMD(args: String*): CmdLike = ExecCmd("CMD", args: _*)

    /**
      * @param args arguments
      * @return ExceCmd
      */
    final def ENTRYPOINT(args: String): CmdLike = ExecCmd("ENTRYPOINT", args)

    /**
      * @param args arguments
      * @return ExceCmd
      */
    final def EXPOSE(args: Int*): CmdLike = Cmd("EXPOSE", args.map(_.toString): _*)

    /**
      * @param path path to directory
      * @return ExceCmd
      */
    final def WORKDIR(path: String): CmdLike = Cmd("WORKDIR", path)

    /**
      * uses the `mappings in Unversial` to generate the
      * `mappings in Docker`.
      */
    def mapGenericFilesToDocker: Seq[Setting[_]] = {
        def renameDests(from: Seq[(File, String)], dest: String) =
            for {
                (f, path) <- from
                newPath = "%s/%s" format(dest, path)
            } yield (f, newPath)

        inConfig(Docker)(Seq(mappings := renameDests((mappings in Universal).value, defaultLinuxInstallLocation.value)))
    }

    private[docker] def publishLocalLogger(log: Logger) =
        new ProcessLogger {
            def error(err: => String): Unit =
                err match {
                    case s if s.startsWith("Uploading context") =>
                        log.debug(s) // pre-1.0
                    case s if s.startsWith("Sending build context") =>
                        log.debug(s) // 1.0
                    case s if !s.trim.isEmpty => log.error(s)
                    case s =>
                }

            def info(inf: => String): Unit = inf match {
                case s if !s.trim.isEmpty => log.info(s)
                case s =>
            }

            def buffer[T](f: => T): T = f
        }

    def publishLocalDocker(context: File, buildCommand: Seq[String], log: Logger): Unit = {
        log.debug("Executing Native " + buildCommand.mkString(" "))
        log.debug(s"Working directory: ${context.getAbsolutePath}")

        val ret = Process(buildCommand, context) ! publishLocalLogger(log)

        if (ret != 0)
            throw new RuntimeException("Nonzero exit value: " + ret)
    }

    def rmiDocker(execCommand: Seq[String], tag: String, log: Logger): Unit = {
        def rmiDockerLogger(log: Logger) = new ProcessLogger {
            def error(err: => String): Unit = err match {
                case s if !s.trim.isEmpty => log.error(s)
                case s =>
            }

            def info(inf: => String): Unit = log.info(inf)

            def buffer[T](f: => T): T = f
        }

        log.debug(s"Removing ${tag}")

        val cmd = execCommand :+ tag
        val ret = Process(cmd) ! rmiDockerLogger(log)

        if (ret != 0)
            sys.error(s"Nonzero exit value: ${ret}")
        else
            log.info(s"Removed image ${tag}")
    }

    def publishDocker(execCommand: Seq[String], tag: String, log: Logger): Unit = {
        val loginRequired = new AtomicBoolean(false)

        def publishLogger(log: Logger) =
            new ProcessLogger {

                def error(err: => String): Unit = err match {
                    case s if !s.trim.isEmpty => log.error(s)
                    case s =>
                }

                def info(inf: => String): Unit =
                    inf match {
                        case s if s.startsWith("Please login") =>
                            loginRequired.compareAndSet(false, true)
                        case s if !loginRequired.get && !s.trim.isEmpty => log.info(s)
                        case s =>
                    }

                def buffer[T](f: => T): T = f
            }

        val cmd = execCommand ++ Seq("push", tag)

        log.debug("Executing " + cmd.mkString(" "))

        val ret = Process(cmd) ! publishLogger(log)

        if (loginRequired.get)
            sys.error("""No credentials for repository, please run "docker login"""")
        else if (ret != 0)
            sys.error("Nonzero exit value: " + ret)
        else
            log.info("Published image " + tag)
    }

}
