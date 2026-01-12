package com.typesafe.sbt.packager
package universal

import sbt.{*, given}
import sbt.Keys.*
import Archives.*
import com.typesafe.sbt.SbtNativePackager
import com.typesafe.sbt.packager.Compat.*
import com.typesafe.sbt.packager.Keys.*
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.validation._
import com.typesafe.sbt.packager.{SettingsHelper, Stager}
import sbt.Keys.TaskStreams
import xsbti.FileConverter

/**
  * ==Universal Plugin==
  *
  * Defines behavior to construct a 'universal' zip for installation.
  *
  * ==Configuration==
  *
  * In order to configure this plugin take a look at the available [[com.typesafe.sbt.packager.universal.UniversalKeys]]
  *
  * @example
  *   Enable the plugin in the `build.sbt`
  *   {{{
  *    enablePlugins(UniversalPlugin)
  *   }}}
  */
object UniversalPlugin extends AutoPlugin {

  object autoImport extends UniversalKeys {
    val Universal: Configuration = config("universal")
    val UniversalDocs: Configuration = config("universal-docs")
    val UniversalSrc: Configuration = config("universal-src")

    /**
      * Use native zipping instead of java based zipping
      */
    def useNativeZip: Seq[Setting[?]] =
      makePackageSettings(packageBin, Universal)(makeNativeZip) ++
        makePackageSettings(packageBin, UniversalDocs)(makeNativeZip) ++
        makePackageSettings(packageBin, UniversalSrc)(makeNativeZip)
  }

  import autoImport._

  override def requires = SbtNativePackager

  override def projectConfigurations: Seq[Configuration] =
    Seq(Universal, UniversalDocs, UniversalSrc)

  override def globalSettings: Seq[Def.Setting[?]] =
    Seq[Setting[?]](
      // Since more than just the docker plugin uses the docker command, we define this in the universal plugin
      // so that it can be configured once and shared by all plugins without requiring the docker plugin.
      DockerPlugin.autoImport.dockerExecCommand := Seq("docker")
    )

  override lazy val buildSettings: Seq[Setting[?]] = Seq[Setting[?]](containerBuildImage := None)

  /** The basic settings for the various packaging types. */
  override lazy val projectSettings: Seq[Setting[?]] = Seq[Setting[?]](
    // For now, we provide delegates from dist/stage to universal...
    dist := (Universal / dist).value,
    stage := (Universal / stage).value,
    // TODO - We may need to do this for UniversalSrcs + UniversalDocs
    Universal / name := name.value,
    UniversalDocs / name := (Universal / name).value,
    UniversalSrc / name := (Universal / name).value,
    Universal / packageName := packageName.value,
    topLevelDirectory := Some((Universal / packageName).value),
    Universal / executableScriptName := executableScriptName.value
  ) ++
    makePackageSettingsForConfig(Universal) ++
    makePackageSettingsForConfig(UniversalDocs) ++
    makePackageSettingsForConfig(UniversalSrc) ++
    defaultUniversalArchiveOptions

  /** Creates all package types for a given configuration */
  private[this] def makePackageSettingsForConfig(config: Configuration): Seq[Setting[?]] =
    makePackageSettings(packageBin, config)(makeZip) ++
      makePackageSettings(packageOsxDmg, config)(makeDmg) ++
      makePackageSettings(packageZipTarball, config)(makeTgz) ++
      makePackageSettings(packageXzTarball, config)(makeTxz) ++
      inConfig(config)(
        Seq(
          packageName := (packageName.value + "-" + version.value),
          mappings := findSources(sourceDirectory.value, fileConverter.value),
          dist := printDist(packageBin.value, streams.value),
          stagingDirectory := target.value / "stage",
          stage := {
            val conv0 = fileConverter.value
            implicit val conv: FileConverter = conv0
            Stager.stage(config.name)(streams.value, stagingDirectory.value, mappings.value)
          }
        )
      ) ++ Seq(
        config / sourceDirectory := sourceDirectory.value / config.name,
        config / validatePackageValidators := validatePackageValidators.value,
        config / target := target.value / config.name
      )

  private[this] def defaultUniversalArchiveOptions: Seq[Setting[?]] =
    Seq(
      Universal / packageZipTarball / universalArchiveOptions := Seq("-pcvf"),
      Universal / packageXzTarball / universalArchiveOptions := Seq("-pcvf"),
      UniversalDocs / packageZipTarball / universalArchiveOptions := Seq("-pcvf"),
      UniversalDocs / packageXzTarball / universalArchiveOptions := Seq("-pcvf"),
      UniversalSrc / packageZipTarball / universalArchiveOptions := Seq("-pcvf"),
      UniversalSrc / packageXzTarball / universalArchiveOptions := Seq("-pcvf")
    )

  private[this] def printDist(dist: PluginCompat.FileRef, streams: TaskStreams): PluginCompat.FileRef = {
    streams.log.info("")
    streams.log.info("Your package is ready in " + dist.toString())
    streams.log.info("")
    dist
  }

  private type Packager =
    (File, String, Seq[(File, String)], Option[String], Seq[String]) => File

  /** Creates packaging settings for a given package key, configuration + archive type. */
  private[this] def makePackageSettings(packageKey: TaskKey[PluginCompat.FileRef], config: Configuration)(
    packager: Packager
  ): Seq[Setting[?]] =
    inConfig(config)(
      Seq(
        packageKey / universalArchiveOptions := Nil,
        packageKey / mappings := mappings.value,
        packageKey := Def.uncached {
          val conv0 = fileConverter.value
          implicit val conv: FileConverter = conv0
          val xs = (packageKey / mappings).value
          val fileMappings = xs.map { case (ref, p) => PluginCompat.toFile(ref) -> p }
          val file = packager(
            target.value,
            packageName.value,
            fileMappings,
            topLevelDirectory.value,
            (packageKey / universalArchiveOptions).value
          )
          PluginCompat.toFileRef(file)
        },
        packageKey / validatePackageValidators := {
          val conv0 = fileConverter.value
          implicit val conv: FileConverter = conv0
          val xs = (packageKey / mappings).value
          val fileMappings = xs.map { case (ref, p) => PluginCompat.toFile(ref) -> p }
          (config / validatePackageValidators).value ++ Seq(
            nonEmptyMappings(fileMappings),
            filesExist(fileMappings),
            checkMaintainer((packageKey / maintainer).value, asWarning = true)
          )
        },
        packageKey / validatePackage := Validation
          .runAndThrow((config / packageKey / validatePackageValidators).value, streams.value.log),
        packageKey := Def.uncached(packageKey.dependsOn(packageKey / validatePackage).value)
      )
    )

  /** Finds all sources in a source directory. */
  private[this] def findSources(sourceDir: File, conv0: FileConverter): Seq[(PluginCompat.FileRef, String)] = {
    implicit val conv: FileConverter = conv0
    ((PathFinder(sourceDir) ** AllPassFilter) --- sourceDir).pair(file => IO.relativize(sourceDir, file)).map {
      case (f, p) => PluginCompat.toFileRef(f) -> p
    }
  }

}

object UniversalDeployPlugin extends AutoPlugin {

  import UniversalPlugin.autoImport._

  override def requires: Plugins = UniversalPlugin

  override def projectSettings: Seq[Setting[?]] =
    SettingsHelper.makeDeploymentSettings(Universal, Universal / packageBin, "zip") ++
      SettingsHelper.addPackage(Universal, Universal / packageZipTarball, "tgz") ++
      SettingsHelper.makeDeploymentSettings(UniversalDocs, Universal / packageBin, "zip") ++
      SettingsHelper.addPackage(UniversalDocs, Universal / packageXzTarball, "txz")
}
