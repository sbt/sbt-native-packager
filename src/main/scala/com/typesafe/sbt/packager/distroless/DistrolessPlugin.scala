package com.typesafe.sbt.packager
package distroless

import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.Keys.{defaultLinuxInstallLocation, maintainer, scriptClasspath}
import com.typesafe.sbt.packager.archetypes.scripts.*
import com.typesafe.sbt.packager.docker.DockerPlugin.UnixSeparatorChar
import com.typesafe.sbt.packager.docker.{Cmd, CmdLike, DockerPlugin, ExecCmd}
import sbt.*
import sbt.Keys.{discoveredMainClasses, mainClass, mappings, streams}

/**
  * ==Distroless Plugin==
  *
  * This plugin helps you build docker containers with distroless images
  *
  * ==Configuration==
  *
  * In order to configure this plugin take a look at the available [[com.typesafe.sbt.packager.docker.DockerKeys]]
  *
  * ==Requirements==
  *
  * You need docker to have docker installed on your system and be able to execute commands. Check with a single
  * command:
  *
  * {{{
  * docker version
  * }}}
  *
  * @example
  *   Enable the plugin in the `build.sbt`
  *   {{{
  *    enablePlugins(DistrolessPlugin)
  *   }}}
  */
object DistrolessPlugin extends AutoPlugin {

  override def requires: Plugins = DockerPlugin

  object autoImport extends Keys

  import DistrolessPlugin.autoImport.*
  import DockerPlugin.autoImport.*

  override def projectConfigurations: Seq[Configuration] = Seq()

  // Some of the default values are now provided in the global setting based on
  // sbt plugin best practice: https://www.scala-sbt.org/release/docs/Plugins-Best-Practices.html#Provide+default+values+in
  override lazy val globalSettings: Seq[Setting[?]] =
    Seq(
      dockerBaseImage := "gcr.io/distroless/java",
      dockerEntrypoint := Seq("/usr/bin/java"),
      distrolessDebuggerPort := None
    )

  override lazy val projectSettings: Seq[Setting[?]] = Seq(dockerCommands := {
    val base = dockerBaseImage.value
    val baseDirectory = (Docker / defaultLinuxInstallLocation).value
    val dependencies = (Docker / scriptClasspath).value
    val debugPort = distrolessDebuggerPort.value
    val compileMainClass = (Compile / mainClass).value
    val compileDiscoveredMainClasses = (Compile / discoveredMainClasses).value
    val log = streams.value.log

    val layerMappings = (Docker / dockerLayerMappings).value
    val layerIdsAscending = layerMappings.map(_.layerId).distinct.sortWith { (a, b) =>
      // Make the None (unspecified) layer the last layer
      a.getOrElse(Int.MaxValue) < b.getOrElse(Int.MaxValue)
    }

    val cmd = if (dockerCmd.value.isEmpty) {
      makeClasspath(baseDirectory, dependencies) ++
        debugPort.map(makeJdwpDebugger).toSeq ++
        defineMainClass(compileMainClass, compileDiscoveredMainClasses, log).toSeq
    } else {
      dockerCmd.value
    }

    (
      makeFromAs(base, "mainstage") +:
        makeMaintainer((Docker / maintainer).value).toSeq :+
        makeWorkdir(baseDirectory)
    ) ++
      layerIdsAscending.map { layerId =>
        makeCopyLayerDirect(layerId, baseDirectory)
      } ++
      dockerLabels.value.map(makeLabel) ++
      dockerEnvVars.value.map(makeEnvVar) ++
      makeExposePorts(dockerExposedPorts.value, dockerExposedUdpPorts.value, distrolessDebuggerPort.value) ++
      Seq(makeEntrypoint(dockerEntrypoint.value), makeCmd(cmd))
  }) ++ mapGenericFilesToDocker

  /**
    * @param dockerBaseImage
    * @param name
    * @return
    *   FROM command
    */
  private final def makeFromAs(dockerBaseImage: String, name: String): CmdLike =
    Cmd("FROM", dockerBaseImage, "AS", name)

  /**
    * @param maintainer
    *   (optional)
    * @return
    *   LABEL MAINTAINER if defined
    */
  private final def makeMaintainer(maintainer: String): Option[CmdLike] =
    if (maintainer.isEmpty) None else Some(makeLabel(Tuple2("MAINTAINER", maintainer)))

  /**
    * @param dockerBaseDirectory
    *   the installation directory
    */
  private final def makeWorkdir(dockerBaseDirectory: String): CmdLike =
    Cmd("WORKDIR", dockerBaseDirectory)

  /**
    * @param label
    * @return
    *   LABEL command
    */
  private final def makeLabel(label: (String, String)): CmdLike = {
    val (variable, value) = label
    Cmd("LABEL", variable + "=\"" + value + "\"")
  }

  /**
    * @param envVar
    * @return
    *   ENV command
    */
  private final def makeEnvVar(envVar: (String, String)): CmdLike = {
    val (variable, value) = envVar
    Cmd("ENV", variable + "=\"" + value + "\"")
  }

  /**
    * @param dockerBaseDirectory
    *   the installation directory
    * @return
    *   COPY command copying all files inside the installation directory
    */
  private final def makeCopyLayerDirect(layerId: Option[Int], dockerBaseDirectory: String): CmdLike = {

    /**
      * This is the file path of the file in the Docker image, and does not depend on the OS where the image is being
      * built. This means that it needs to be the Unix file separator even when the image is built on e.g. Windows
      * systems.
      */
    val files = dockerBaseDirectory.split(UnixSeparatorChar)(1)
    val path = layerId.map(i => s"$i/$files").getOrElse(s"$files")
    Cmd("COPY", s"$path /$files")
  }

  /**
    * @param exposedPorts
    * @return
    *   if ports are exposed the EXPOSE command
    */
  private final def makeExposePorts(
    exposedPorts: Seq[Int],
    exposedUdpPorts: Seq[Int],
    jdwpPort: Option[Int]
  ): Option[CmdLike] =
    if (exposedPorts.isEmpty && exposedUdpPorts.isEmpty && jdwpPort.isEmpty) None
    else
      Some(
        Cmd(
          "EXPOSE",
          (exposedPorts.map(_.toString) ++ exposedUdpPorts.map(_.toString).map(_ + "/udp") ++ jdwpPort
            .map(_.toString)
            .toSeq) mkString " "
        )
      )

  /**
    * @param entrypoint
    * @return
    *   ENTRYPOINT command
    */
  private final def makeEntrypoint(entrypoint: Seq[String]): CmdLike =
    ExecCmd("ENTRYPOINT", entrypoint*)

  /**
    * Default CMD implementation as default parameters to ENTRYPOINT.
    * @param args
    * @return
    *   CMD with args in exec form
    */
  private final def makeCmd(args: Seq[String]): CmdLike =
    ExecCmd("CMD", args*)

  /**
    * Builds the classpath argument to use when running java. This works by concatenating all dependencies in the lib
    * folder with a colon
    * @param baseDirectory
    *   where is the base docker directory (ie. /opt/docker)
    * @param dependencies
    *   list of dependencies as JAR files
    * @return
    *   The <pre>-cp</pre> argument with all dependencies
    */
  private def makeClasspath(baseDirectory: String, dependencies: Seq[String]): Seq[String] = {
    val dependenciesBaseDirectory = baseDirectory + "/lib/"

    Seq("-cp", dependencies.map(dependenciesBaseDirectory + _).mkString(":"))
  }

  /**
    * Builds the JDWP cli argument to be used when running java
    * @param port
    *   port to expose the remote debugger on
    * @return
    */
  private def makeJdwpDebugger(port: Int): String =
    s"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$port"

  /**
    * Tries to define what main class to call when running the distroless container
    * @param mainClass
    *   optional main class
    * @param discoveredMainClasses
    *   discovered main classes
    * @return
    */
  private def defineMainClass(
    mainClass: Option[String],
    discoveredMainClasses: Seq[String],
    log: sbt.Logger
  ): Option[String] =
    StartScriptMainClassConfig.from(mainClass, discoveredMainClasses) match {
      case SingleMain(main) =>
        Some(main)
      case ExplicitMainWithAdditional(main, _) =>
        Some(main)
      case NoMain =>
        log.warn("You have no main class in your project. The resulting distroless container will fail when running.")
        None
      case MultipleMains(_) =>
        log.warn(
          "You have multiple main class in your project. The resulting distroless container needs to know which main class to run at build time."
        )
        None
    }

  /**
    * Uses the `Universal / mappings` to generate the `Docker / mappings`. This overrides the `Docker / mappings` to
    * skip scripts
    */
  def mapGenericFilesToDocker: Seq[Setting[?]] = {
    def renameDests(from: Seq[(PluginCompat.FileRef, String)], dest: String) =
      for {
        // Distroless, unlike Docker, cannot run any script, so don't include it in the mappings
        (f, path) <- from if !path.startsWith("bin/")
        pathWithValidSeparator = if (Path.sep == '/') path else path.replace(Path.sep, '/')
        newPath = "%s/%s" format (dest, pathWithValidSeparator)
      } yield (f, newPath)

    inConfig(Docker)(Seq(mappings := renameDests((Universal / mappings).value, defaultLinuxInstallLocation.value)))
  }
}
