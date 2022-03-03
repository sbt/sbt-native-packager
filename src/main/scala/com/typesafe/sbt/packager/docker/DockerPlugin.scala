package com.typesafe.sbt.packager.docker

import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.archetypes.jar.ClasspathJarPlugin
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.{daemonUser, defaultLinuxInstallLocation}
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.stage
import com.typesafe.sbt.packager.validation._
import com.typesafe.sbt.packager.{MappingsHelper, Stager}
import sbt.Keys._
import sbt._

import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
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

  object autoImport extends DockerKeysEx {
    val Docker: Configuration = config("docker")

    // IntelliJ dies with the proper type annotation ¯\_(ツ)_/¯
    val DockerAlias = com.typesafe.sbt.packager.docker.DockerAlias
  }

  import autoImport._

  /**
    * The separator used by makeCopyLayerIntermediate should be always forced to UNIX separator.
    * The separator doesn't depend on the OS where Dockerfile is being built.
    */
  val UnixSeparatorChar = '/'

  override def requires: Plugins = UniversalPlugin

  override def projectConfigurations: Seq[Configuration] = Seq(Docker)

  // Some of the default values are now provided in the global setting based on
  // sbt plugin best practice: https://www.scala-sbt.org/release/docs/Plugins-Best-Practices.html#Provide+default+values+in
  override lazy val globalSettings: Seq[Setting[_]] = Seq(
    // See https://github.com/sbt/sbt-native-packager/issues/1187
    // Note: Do not make this setting depend on the Docker version.
    // Docker version may change depending on the person running the build, or even with something like
    // `eval $(minikube docker-env)`.
    // The Docker image the build creates should be repeatable regardless of who runs it as much as possible.
    // Instead of making dockerPermissionStrategy dependent on the Docker version, what we do instead is to
    // run validation, and warn the build users if the strategy is not compatible with `docker` that's in scope.
    dockerPermissionStrategy := DockerPermissionStrategy.MultiStage,
    dockerChmodType := DockerChmodType.UserGroupReadExecute,
    dockerBaseImage := "openjdk:8",
    dockerExposedPorts := Seq(),
    dockerExposedUdpPorts := Seq(),
    dockerExposedVolumes := Seq(),
    dockerLabels := Map(),
    dockerEnvVars := Map(),
    dockerRepository := None,
    dockerUsername := None,
    dockerUpdateLatest := false,
    dockerAutoremoveMultiStageIntermediateImages := true,
    dockerCmd := Seq(),
    dockerCommandsPrepend := Map()
  )

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    dockerAlias := DockerAlias(
      (Docker / dockerRepository).value,
      (Docker / dockerUsername).value,
      (Docker / packageName).value,
      Option((Docker / version).value)
    ),
    dockerLayerGrouping := { _: String =>
      None
    },
    dockerGroupLayers := {
      val dockerBaseDirectory = (Docker / defaultLinuxInstallLocation).value
      // Ensure this doesn't break even if the JvmPlugin isn't enabled.
      var artifacts = projectDependencyArtifacts.?.value.getOrElse(Nil).map(_.data).toSet

      // add the classpath jar to the project artifacts to improve layer caching as it is created by
      // the ClasspathJarPlugin and is not part of the projectDependencyArtifacts
      ClasspathJarPlugin.autoImport.packageJavaClasspathJar.?.value match {
        case Some(p) => artifacts += p
        case _       =>
      }

      val oldFunction = dockerLayerGrouping.value

      // By default we set this to a function that always returns None.
      val oldPartialFunction = Function.unlift((tuple: (File, String)) => oldFunction(tuple._2))

      val libDir = dockerBaseDirectory + "/lib/"
      val binDir = dockerBaseDirectory + "/bin/"
      val jreDir = dockerBaseDirectory + "/jre/"
      val confDir = dockerBaseDirectory + "/conf/"

      oldPartialFunction.orElse {
        // bin directory contains start scripts which are containing artifacts / classpath jar,
        // so should be together with actual artifacts
        case (file, path) if artifacts(file) || path.startsWith(binDir) => 4
        case (_, path) if path.startsWith(jreDir)                       => 3
        case (_, path) if path.startsWith(libDir)                       => 2
        case (_, path) if path.startsWith(confDir)                      => 1
      }
    },
    dockerAliases := {
      val alias = dockerAlias.value
      if (dockerUpdateLatest.value)
        Seq(alias, alias.withTag(Option("latest")))
      else
        Seq(alias)
    },
    dockerEntrypoint := Seq(s"${(Docker / defaultLinuxInstallLocation).value}/bin/${executableScriptName.value}"),
    dockerVersion := Try(
      Process(dockerExecCommand.value ++ Seq("version", "--format", "'{{.Server.Version}}'")).!!
    ).toOption
      .map(_.trim)
      .flatMap(DockerVersion.parse),
    dockerApiVersion := Try(
      Process(dockerExecCommand.value ++ Seq("version", "--format", "'{{.Server.APIVersion}}'")).!!
    ).toOption
      .map(_.trim)
      .flatMap(DockerApiVersion.parse),
    dockerBuildInit := false,
    dockerBuildOptions := Seq("--force-rm") ++ dockerAliases.value.flatMap { alias =>
      Seq("-t", alias.toString)
    } ++ { if (dockerBuildInit.value) List("--init") else Nil },
    dockerRmiCommand := dockerExecCommand.value ++ Seq("rmi"),
    dockerBuildCommand := dockerExecCommand.value ++ Seq("build") ++ dockerBuildOptions.value ++ Seq("."),
    dockerAdditionalPermissions := {
      val basePath = (Docker / defaultLinuxInstallLocation).value
      (Docker / dockerLayerMappings).value
        .collect {
          // by default we assume everything in the bin/ folder should be executable that is not a .bat file
          case LayeredMapping(_, _, path) if path.startsWith(s"$basePath/bin/") && !path.endsWith(".bat") =>
            DockerChmodType.UserGroupPlusExecute -> path
          // sh files should also be marked as executable
          case LayeredMapping(_, _, path) if path.endsWith(".sh") =>
            DockerChmodType.UserGroupPlusExecute -> path
        }
    },
    dockerCommands := {
      val strategy = dockerPermissionStrategy.value
      val dockerBaseDirectory = (Docker / defaultLinuxInstallLocation).value
      val user = (Docker / daemonUser).value
      val uidOpt = (Docker / daemonUserUid).value
      val group = (Docker / daemonGroup).value
      val gidOpt = (Docker / daemonGroupGid).value
      val base = dockerBaseImage.value
      val addPerms = dockerAdditionalPermissions.value
      val multiStageId = UUID.randomUUID().toString
      val generalCommands = makeFromAs(base, "mainstage") +: makeMaintainer((Docker / maintainer).value).toSeq
      val stage0name = "stage0"
      val layerMappings = (Docker / dockerLayerMappings).value
      val layerIdsAscending = layerMappings.map(_.layerId).distinct.sortWith { (a, b) =>
        // Make the None (unspecified) layer the last layer
        a.getOrElse(Int.MaxValue) < b.getOrElse(Int.MaxValue)
      }
      val stage0: Seq[CmdLike] = strategy match {
        case DockerPermissionStrategy.MultiStage =>
          Seq(
            makeFromAs(base, stage0name),
            makeLabel("snp-multi-stage" -> "intermediate"),
            makeLabel("snp-multi-stage-id" -> multiStageId),
            makeWorkdir(dockerBaseDirectory)
          ) ++
            layerIdsAscending.map(l => makeCopyLayerIntermediate(l, dockerBaseDirectory)) ++
            Seq(makeUser("root")) ++ layerIdsAscending.map(
            l => makeChmodRecursive(dockerChmodType.value, Seq(pathInLayer(dockerBaseDirectory, l)))
          ) ++ {
            val layerToPath = (Docker / dockerGroupLayers).value
            addPerms map {
              case (tpe, v) =>
                // Try and find the source file for the path from the mappings
                val layerId = layerMappings
                  .find(_.path == v)
                  .map(_.layerId)
                  .getOrElse {
                    // We couldn't find a source file for the mapping, so try with a dummy source file,
                    // in case there is an explicitly configured path based layer mapping, eg for a directory.
                    layerToPath.lift((new File("/dev/null"), v))
                  }
                makeChmod(tpe, Seq(pathInLayer(v, layerId)))
            }
          } ++
            Seq(DockerStageBreak)
        case _ => Seq()
      }

      val stage1: Seq[CmdLike] = generalCommands ++
        (uidOpt match {
          case Some(_) => Seq(makeUser("root"), makeUserAdd(user, group, uidOpt, gidOpt))
          case _       => Seq()
        }) ++
        (Docker / dockerCommandsPrepend).value.getOrElse("test", Seq()) ++
        Seq(makeWorkdir(dockerBaseDirectory)) ++ {
        (strategy match {
          case DockerPermissionStrategy.MultiStage =>
            layerIdsAscending.map { layerId =>
              makeCopyFrom(pathInLayer(dockerBaseDirectory, layerId), dockerBaseDirectory, stage0name, user, group)
            }
          case DockerPermissionStrategy.Run =>
            layerIdsAscending.map(layerId => makeCopyLayerDirect(layerId, dockerBaseDirectory)) ++
              Seq(makeChmodRecursive(dockerChmodType.value, Seq(dockerBaseDirectory))) ++
              (addPerms map { case (tpe, v) => makeChmod(tpe, Seq(v)) })
          case DockerPermissionStrategy.CopyChown =>
            layerIdsAscending.map(layerId => makeCopyChown(layerId, dockerBaseDirectory, user, group))
          case DockerPermissionStrategy.None =>
            layerIdsAscending.map(layerId => makeCopyLayerDirect(layerId, dockerBaseDirectory))
        })
      } ++
        dockerLabels.value.map(makeLabel) ++
        dockerEnvVars.value.map(makeEnvVar) ++
        makeExposePorts(dockerExposedPorts.value, dockerExposedUdpPorts.value) ++
        makeVolumes(dockerExposedVolumes.value, user, group) ++
        Seq(uidOpt match {
          case Some(uid) => makeUser(uid, gidOpt)
          case _         => makeUser(user)
        }) ++
        // Use this to debug permissions
        // Seq(ExecCmd("RUN", Seq("ls", "-l", "/opt/docker/bin/"): _*)) ++
        Seq(makeEntrypoint(dockerEntrypoint.value), makeCmd(dockerCmd.value))

      stage0 ++ stage1
    }
  ) ++ mapGenericFilesToDocker ++ inConfig(Docker)({

    def publishLocalTask =
      Def.task {
        val log = streams.value.log
        publishLocalDocker(
          stage.value,
          dockerBuildCommand.value,
          dockerExecCommand.value,
          dockerPermissionStrategy.value,
          dockerAutoremoveMultiStageIntermediateImages.value,
          log
        )
        log.info(
          s"Built image ${dockerAlias.value.withTag(None).toString} with tags [${dockerAliases.value.flatMap(_.tag).mkString(", ")}]"
        )
      } tag (Tags.Disk, Tags.Publish)

    def publishTask =
      Def.task {
        val _ = publishLocal.value
        val alias = dockerAliases.value
        val log = streams.value.log
        val execCommand = dockerExecCommand.value
        alias.foreach { aliasValue =>
          publishDocker(execCommand, aliasValue.toString, log)
        }
      } tag (Tags.Network, Tags.Publish)

    def cleanTask =
      Def.task {
        val alias = dockerAliases.value
        val log = streams.value.log
        val rmiCommand = dockerRmiCommand.value
        // clean up images
        alias.foreach { aliasValue =>
          rmiDocker(rmiCommand, aliasValue.toString, log)
        }
      }

    Seq(
      executableScriptName := executableScriptName.value,
      mappings ++= dockerPackageMappings.value,
      name := name.value,
      packageName := packageName.value,
      publishLocal := publishLocalTask.value,
      publish := publishTask.value,
      clean := cleanTask.value,
      sourceDirectory := sourceDirectory.value / "docker",
      stage := Stager.stage(Docker.name)(
        streams.value,
        stagingDirectory.value,
        dockerLayerMappings.value.map {
          case LayeredMapping(layerIdx, file, path) => (file, pathInLayer(path, layerIdx))
        }
      ),
      stage := (stage dependsOn dockerGenerateConfig).value,
      stagingDirectory := (Docker / target).value / "stage",
      dockerLayerMappings := {
        val dockerGroups = dockerGroupLayers.value
        val dockerFinalFiles = (Docker / mappings).value
        for {
          mapping @ (file, path) <- dockerFinalFiles
          layerIdx = dockerGroups.lift(mapping)
        } yield LayeredMapping(layerIdx, file, path)
      },
      target := target.value / "docker",
      // pick a user name that's unlikely to exist in base images
      daemonUser := "demiourgos728",
      // when daemonUserUid is set, we will try to create this user and set numeric USER
      daemonUserUid := Some("1001"),
      daemonGroup := "root",
      daemonGroupGid := Some("0"),
      defaultLinuxInstallLocation := "/opt/docker",
      validatePackage := Validation
        .runAndThrow(validatePackageValidators.value, streams.value.log),
      validatePackageValidators := Seq(
        nonEmptyMappings((Docker / mappings).value),
        filesExist((Docker / mappings).value),
        validateExposedPorts(dockerExposedPorts.value, dockerExposedUdpPorts.value),
        validateDockerVersion(dockerApiVersion.value),
        validateDockerPermissionStrategy(dockerPermissionStrategy.value, dockerVersion.value, dockerApiVersion.value)
      ),
      dockerPackageMappings := MappingsHelper.contentOf(sourceDirectory.value),
      dockerGenerateConfig := {
        val _ = validatePackage.value
        generateDockerConfig(dockerCommands.value, stagingDirectory.value)
      }
    )
  })

  /**
    * @param maintainer (optional)
    * @return LABEL MAINTAINER if defined
    */
  private final def makeMaintainer(maintainer: String): Option[CmdLike] =
    if (maintainer.isEmpty) None else Some(makeLabel(Tuple2("MAINTAINER", maintainer)))

  /**
    * @param dockerBaseImage
    * @param name
    * @return FROM command
    */
  private final def makeFromAs(dockerBaseImage: String, name: String): CmdLike =
    Cmd("FROM", dockerBaseImage, "as", name)

  /**
    * @param label
    * @return LABEL command
    */
  private final def makeLabel(label: (String, String)): CmdLike = {
    val (variable, value) = label
    Cmd("LABEL", variable + "=\"" + value + "\"")
  }

  /**
    * @param envVar
    * @return ENV command
    */
  private final def makeEnvVar(envVar: (String, String)): CmdLike = {
    val (variable, value) = envVar
    Cmd("ENV", variable + "=\"" + value + "\"")
  }

  /**
    * @param dockerBaseDirectory, the installation directory
    */
  private final def makeWorkdir(dockerBaseDirectory: String): CmdLike =
    Cmd("WORKDIR", dockerBaseDirectory)

  /**
    * @param dockerBaseDirectory the installation directory
    * @return COPY command copying all files inside the installation directory
    */
  private final def makeCopyLayerDirect(layerId: Option[Int], dockerBaseDirectory: String): CmdLike = {

    /**
      * This is the file path of the file in the Docker image, and does not depend on the OS where the image
      * is being built. This means that it needs to be the Unix file separator even when the image is built
      * on e.g. Windows systems.
      */
    val files = dockerBaseDirectory.split(UnixSeparatorChar)(1)
    val path = layerId.map(i => s"$i/$files").getOrElse(s"$files")
    Cmd("COPY", s"$path /$files")
  }

  private final def makeCopyLayerIntermediate(layerId: Option[Int], dockerBaseDirectory: String): CmdLike = {
    val files = dockerBaseDirectory.split(UnixSeparatorChar)(1)
    val path = layerId.map(i => s"$i/$files").getOrElse(s"$files")
    Cmd("COPY", s"$path /$path")
  }

  /**
    * @param src the installation directory
    * @param stage files are copied from the given build stage
    * @param daemonUser
    * @param daemonGroup
    * @return COPY command copying all files inside the directory from another build stage.
    */
  private final def makeCopyFrom(
    src: String,
    dest: String,
    stage: String,
    daemonUser: String,
    daemonGroup: String
  ): CmdLike =
    Cmd("COPY", s"--from=$stage --chown=$daemonUser:$daemonGroup $src $dest")

  /**
    * @param layerId the intermediate layer
    * @param daemonUser
    * @param daemonGroup
    * @return COPY command copying all files inside the directory from another build stage.
    */
  private final def makeCopyChown(
    layerId: Option[Int],
    dockerBaseDirectory: String,
    daemonUser: String,
    daemonGroup: String
  ): CmdLike = {

    /**
      * This is the file path of the file in the Docker image, and does not depend on the OS where the image
      * is being built. This means that it needs to be the Unix file separator even when the image is built
      * on e.g. Windows systems.
      */
    val files = dockerBaseDirectory.split(UnixSeparatorChar)(1)
    val path = layerId.map(i => s"$i/$files").getOrElse(s"$files")
    Cmd("COPY", s"--chown=$daemonUser:$daemonGroup $path /$files")
  }

  /**
    * @param daemonUser
    * @param daemonGroup
    * @return chown command, owning the installation directory with the daemonuser
    */
  private final def makeChown(daemonUser: String, daemonGroup: String, directories: Seq[String]): CmdLike =
    ExecCmd("RUN", Seq("chown", "-R", s"$daemonUser:$daemonGroup") ++ directories: _*)

  /**
    * @return chmod command
    */
  private final def makeChmod(chmodType: DockerChmodType, files: Seq[String]): CmdLike =
    ExecCmd("RUN", Seq("chmod", chmodType.argument) ++ files: _*)

  /**
    * @return chmod command recursively
    */
  private final def makeChmodRecursive(chmodType: DockerChmodType, directories: Seq[String]): CmdLike =
    ExecCmd("RUN", Seq("chmod", "-R", chmodType.argument) ++ directories: _*)

  /**
    * @param daemonUser
    * @param daemonGroup
    * @param uidOpt
    * @param gidOpt
    * @return useradd to create the daemon user with the given uidOpt and gidOpt after invoking groupadd to
    *         create the daemon group if the given gidOpt does not exists.
    */
  private final def makeUserAdd(
    daemonUser: String,
    daemonGroup: String,
    uidOpt: Option[String],
    gidOpt: Option[String]
  ): CmdLike =
    Cmd(
      "RUN",
      (List("id", "-u", daemonUser, "1>/dev/null", "2>&1", "||") :::
        (gidOpt.fold[List[String]](Nil)(
          gid =>
            List("((", "getent", "group", gid, "1>/dev/null", "2>&1", "||") :::
              List("(", "type", "groupadd", "1>/dev/null", "2>&1", "&&") :::
              List("groupadd", "-g", gid, daemonGroup, "||") :::
              List("addgroup", "-g", gid, "-S", daemonGroup, "))", "&&")
        )) :::
        List("(", "type", "useradd", "1>/dev/null", "2>&1", "&&") :::
        List("useradd", "--system", "--create-home") :::
        (uidOpt.fold[List[String]](Nil)(List("--uid", _))) :::
        (gidOpt.fold[List[String]](Nil)(List("--gid", _))) :::
        List(daemonUser, "||") :::
        List("adduser", "-S") :::
        (uidOpt.fold[List[String]](Nil)(List("-u", _))) :::
        List("-G", daemonGroup, daemonUser, "))")): _*
    )

  /**
    * @param daemonUser
    * @param daemonGroupOpt
    * @return USER docker command
    */
  private final def makeUser(daemonUser: String, daemonGroupOpt: Option[String] = None): CmdLike =
    Cmd("USER", daemonGroupOpt.fold(daemonUser)(daemonUser + ":" + _))

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
    * `Docker / mappings`.
    */
  def mapGenericFilesToDocker: Seq[Setting[_]] = {
    def renameDests(from: Seq[(File, String)], dest: String) =
      for {
        (f, path) <- from
        pathWithValidSeparator = if (Path.sep == '/') path else path.replace(Path.sep, '/')
        newPath = "%s/%s" format (dest, pathWithValidSeparator)
      } yield (f, newPath)

    inConfig(Docker)(Seq(mappings := renameDests((mappings in Universal).value, defaultLinuxInstallLocation.value)))
  }

  private final def pathInLayer(path: String, layer: Option[Int]) = layer.map(i => s"/$i$path").getOrElse(path)

  private[packager] def publishLocalLogger(log: Logger) =
    new sys.process.ProcessLogger {
      override def err(err: => String): Unit =
        err match {
          case s if s.startsWith("Uploading context") =>
            log.debug(s) // pre-1.0
          case s if s.startsWith("Sending build context") =>
            log.debug(s) // 1.0
          case s if s.trim.nonEmpty => log.error(s)
          case _                    =>
        }

      override def out(inf: => String): Unit =
        inf match {
          case s if s.trim.nonEmpty => log.info(s)
          case _                    =>
        }

      override def buffer[T](f: => T): T = f
    }

  // BuildKit ouputs everything to stderr, so we redirect it
  private[this] def publishLocalBuildkitLogger(log: Logger) =
    new sys.process.ProcessLogger {
      override def err(err: => String): Unit =
        err match {
          case s if s.trim.nonEmpty => log.info(s)
          case _                    =>
        }

      override def out(inf: => String): Unit =
        inf match {
          case s if s.trim.nonEmpty => log.info(s)
          case _                    =>
        }

      override def buffer[T](f: => T): T = f
    }

  def publishLocalDocker(
    context: File,
    buildCommand: Seq[String],
    execCommand: Seq[String],
    strategy: DockerPermissionStrategy,
    removeIntermediateImages: Boolean,
    log: Logger
  ): Unit = {
    log.debug("Executing Native " + buildCommand.mkString(" "))
    log.debug("Working directory " + context.toString)

    val logger = sys.env
      .get("DOCKER_BUILDKIT")
      .filter(_ == "1")
      .map(_ => publishLocalBuildkitLogger(log))
      .getOrElse(publishLocalLogger(log))
    val ret = sys.process.Process(buildCommand, context) ! logger

    if (removeIntermediateImages) {
      val labelKey = "snp-multi-stage-id"
      val labelCmd = s"LABEL ${labelKey}="
      strategy match {
        case DockerPermissionStrategy.MultiStage =>
          IO.readLines(context / "Dockerfile")
            .find(_.startsWith(labelCmd))
            .map(_.substring(labelCmd.length).stripPrefix("\"").stripSuffix("\"")) match {
            // No matter if the build process succeeded or failed, we try to remove the intermediate images
            case Some(id) =>
              val label = s"${labelKey}=${id}"
              log.info(s"""Removing intermediate image(s) (labeled "${label}") """)
              val retImageClean =
                sys.process.Process(execCommand ++ s"image prune -f --filter label=${label}".split(" ")) ! logger
              // FYI: "docker image prune" returns 0 (success) no matter if images were removed or not
              if (retImageClean != 0)
                log.error(
                  "Something went wrong while removing multi-stage intermediate image(s)"
                ) // no exception, just let the user know
            case None =>
              log.info(
                """Not removing multi-stage intermediate image(s) because id is missing in Dockerfile (Comment: "# id=...")"""
              )
          }
        case _ => // Intermediate images are not generated when using other strategies
      }
    }

    if (ret != 0)
      throw new RuntimeException("Nonzero exit value: " + ret)
  }

  def rmiDocker(execCommand: Seq[String], tag: String, log: Logger): Unit = {
    def rmiDockerLogger(log: Logger) =
      new sys.process.ProcessLogger {
        override def err(err: => String): Unit =
          err match {
            case s if s.trim.nonEmpty => log.error(s)
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

        override def err(err: => String): Unit =
          err match {
            case s if s.trim.nonEmpty => log.error(s)
            case s                    =>
          }

        override def out(inf: => String): Unit =
          inf match {
            case s if s.startsWith("Please login") =>
              loginRequired.compareAndSet(false, true)
            case s if !loginRequired.get && s.trim.nonEmpty => log.info(s)
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

  private[this] def validateExposedPorts(ports: Seq[Int], udpPorts: Seq[Int]): Validation.Validator =
    () => {
      if (ports.isEmpty && udpPorts.isEmpty)
        List(
          ValidationWarning(
            description = "There are no exposed ports for your docker image",
            howToFix = """| Configure the `dockerExposedPorts` or `dockerExposedUdpPorts` setting. E.g.
             |
             | // standard tcp ports
             | dockerExposedPorts ++= Seq(9000, 9001)
             |
             | // for udp ports
             | dockerExposedUdpPorts += 4444
          """.stripMargin
          )
        )
      else
        List.empty
    }

  private[this] def validateDockerVersion(dockerApiVersion: Option[DockerApiVersion]): Validation.Validator =
    () => {
      dockerApiVersion match {
        case Some(_) => List.empty
        case None =>
          List(
            ValidationWarning(
              description =
                "sbt-native-packager wasn't able to identify the docker version. Some features may not be enabled",
              howToFix = """|sbt-native packager tries to parse the `docker version` output. This can fail if
             |
             |  - the output has changed:
             |    $ docker version --format '{{.Server.APIVersion}}'
             |
             |  - no `docker` executable is available
             |    $ which docker
             |
             |  - you have not the required privileges to run `docker`
             |
             |You can display the parsed docker version in the sbt console with:
             |
             |  sbt:your-project> show dockerApiVersion
             |
             |As a last resort you could hard code the docker version, but it's not recommended!!
             |
             |  import com.typesafe.sbt.packager.docker.DockerApiVersion
             |  dockerApiVersion := Some(DockerApiVersion(1, 40))
          """.stripMargin
            )
          )
      }
    }

  private[this] def validateDockerPermissionStrategy(
    strategy: DockerPermissionStrategy,
    dockerVersion: Option[DockerVersion],
    dockerApiVersion: Option[DockerApiVersion]
  ): Validation.Validator =
    () => {
      (strategy, dockerVersion, dockerApiVersion) match {
        case (DockerPermissionStrategy.MultiStage, Some(ver), Some(apiVer)) if !DockerSupport.multiStage(ver, apiVer) =>
          List(
            ValidationError(
              description =
                s"The detected Docker version $ver is not compatible with DockerPermissionStrategy.MultiStage",
              howToFix = """|sbt-native packager tries to parse the `docker version` output.
             |To use multi-stage build, upgrade your Docker, pick another strategy, or override dockerApiVersion:
             |
             |  import com.typesafe.sbt.packager.docker.DockerPermissionStrategy
             |  dockerPermissionStrategy := DockerPermissionStrategy.Run
             |
             |  import com.typesafe.sbt.packager.docker.DockerApiVersion
             |  dockerApiVersion := Some(DockerApiVersion(1, 40))
          """.stripMargin
            )
          )
        case (DockerPermissionStrategy.CopyChown, Some(ver), Some(apiVer)) if !DockerSupport.chownFlag(ver, apiVer) =>
          List(
            ValidationError(
              description =
                s"The detected Docker version $ver is not compatible with DockerPermissionStrategy.CopyChown",
              howToFix = """|sbt-native packager tries to parse the `docker version` output.
             |To use --chown flag, upgrade your Docker, pick another strategy, or override dockerApiVersion:
             |
             |  import com.typesafe.sbt.packager.docker.DockerPermissionStrategy
             |  dockerPermissionStrategy := DockerPermissionStrategy.Run
             |
             |  import com.typesafe.sbt.packager.docker.DockerApiVersion
             |  dockerApiVersion := Some(DockerApiVersion(1, 40))
          """.stripMargin
            )
          )
        case _ => List.empty
      }
    }
}
