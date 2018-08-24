package com.typesafe.sbt.packager.universal

import sbt._
import sbt.Keys._
import Archives._
import com.typesafe.sbt.SbtNativePackager
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.validation._
import com.typesafe.sbt.packager.{SettingsHelper, Stager}
import sbt.Keys.TaskStreams

/**
  * == Universal Plugin ==
  *
  * Defines behavior to construct a 'universal' zip for installation.
  *
  * == Configuration ==
  *
  * In order to configure this plugin take a look at the available [[com.typesafe.sbt.packager.universal.UniversalKeys]]
  *
  * @example Enable the plugin in the `build.sbt`
  * {{{
  *    enablePlugins(UniversalPlugin)
  * }}}
  */
object UniversalPlugin extends AutoPlugin {

  object autoImport extends UniversalKeys {
    val Universal: Configuration = config("universal")
    val UniversalDocs: Configuration = config("universal-docs")
    val UniversalSrc: Configuration = config("universal-src")

    /**
      * Use native zipping instead of java based zipping
      */
    def useNativeZip: Seq[Setting[_]] =
      makePackageSettings(packageBin, Universal)(makeNativeZip) ++
        makePackageSettings(packageBin, UniversalDocs)(makeNativeZip) ++
        makePackageSettings(packageBin, UniversalSrc)(makeNativeZip)
  }

  import autoImport._

  override def requires = SbtNativePackager

  override def projectConfigurations: Seq[Configuration] =
    Seq(Universal, UniversalDocs, UniversalSrc)

  /** The basic settings for the various packaging types. */
  override lazy val projectSettings: Seq[Setting[_]] = Seq[Setting[_]](
    // For now, we provide delegates from dist/stage to universal...
    dist := (dist in Universal).value,
    stage := (stage in Universal).value,
    // TODO - We may need to do this for UniversalSrcs + UnviersalDocs
    name in Universal := name.value,
    name in UniversalDocs := (name in Universal).value,
    name in UniversalSrc := (name in Universal).value,
    packageName in Universal := packageName.value,
    topLevelDirectory := Some((packageName in Universal).value),
    executableScriptName in Universal := executableScriptName.value
  ) ++
    makePackageSettingsForConfig(Universal) ++
    makePackageSettingsForConfig(UniversalDocs) ++
    makePackageSettingsForConfig(UniversalSrc) ++
    defaultUniversalArchiveOptions

  /** Creates all package types for a given configuration */
  private[this] def makePackageSettingsForConfig(config: Configuration): Seq[Setting[_]] =
    makePackageSettings(packageBin, config)(makeZip) ++
      makePackageSettings(packageOsxDmg, config)(makeDmg) ++
      makePackageSettings(packageZipTarball, config)(makeTgz) ++
      makePackageSettings(packageXzTarball, config)(makeTxz) ++
      inConfig(config)(
        Seq(
          packageName := (packageName.value + "-" + version.value),
          mappings := findSources(sourceDirectory.value),
          dist := printDist(packageBin.value, streams.value),
          stagingDirectory := target.value / "stage",
          stage := Stager.stage(config.name)(streams.value, stagingDirectory.value, mappings.value)
        )
      ) ++ Seq(
      sourceDirectory in config := sourceDirectory.value / config.name,
      validatePackageValidators in config := validatePackageValidators.value,
      target in config := target.value / config.name
    )

  private[this] def defaultUniversalArchiveOptions: Seq[Setting[_]] = Seq(
    universalArchiveOptions in (Universal, packageZipTarball) := Seq("-pcvf"),
    universalArchiveOptions in (Universal, packageXzTarball) := Seq("-pcvf"),
    universalArchiveOptions in (UniversalDocs, packageZipTarball) := Seq("-pcvf"),
    universalArchiveOptions in (UniversalDocs, packageXzTarball) := Seq("-pcvf"),
    universalArchiveOptions in (UniversalSrc, packageZipTarball) := Seq("-pcvf"),
    universalArchiveOptions in (UniversalSrc, packageXzTarball) := Seq("-pcvf")
  )

  private[this] def printDist(dist: File, streams: TaskStreams): File = {
    streams.log.info("")
    streams.log.info("Your package is ready in " + dist.getCanonicalPath)
    streams.log.info("")
    dist
  }

  private type Packager = (File, String, Seq[(File, String)], Option[String], Seq[String]) => File

  /** Creates packaging settings for a given package key, configuration + archive type. */
  private[this] def makePackageSettings(packageKey: TaskKey[File],
                                        config: Configuration)(packager: Packager): Seq[Setting[_]] =
    inConfig(config)(
      Seq(
        universalArchiveOptions in packageKey := Nil,
        mappings in packageKey := mappings.value,
        packageKey := packager(
          target.value,
          packageName.value,
          (mappings in packageKey).value,
          topLevelDirectory.value,
          (universalArchiveOptions in packageKey).value
        ),
        validatePackageValidators in packageKey := (validatePackageValidators in config).value ++ Seq(
          nonEmptyMappings((mappings in packageKey).value),
          filesExist((mappings in packageKey).value),
          checkMaintainer((maintainer in packageKey).value, asWarning = true)
        ),
        validatePackage in packageKey := Validation
          .runAndThrow(validatePackageValidators.in(config, packageKey).value, streams.value.log),
        packageKey := packageKey.dependsOn(validatePackage in packageKey).value
      )
    )

  /** Finds all sources in a source directory. */
  private[this] def findSources(sourceDir: File): Seq[(File, String)] =
    ((PathFinder(sourceDir) ** AllPassFilter) --- sourceDir).pair(file => IO.relativize(sourceDir, file))

}

object UniversalDeployPlugin extends AutoPlugin {

  import UniversalPlugin.autoImport._

  override def requires = UniversalPlugin

  override def projectSettings: Seq[Setting[_]] =
    SettingsHelper.makeDeploymentSettings(Universal, packageBin in Universal, "zip") ++
      SettingsHelper.addPackage(Universal, packageZipTarball in Universal, "tgz") ++
      SettingsHelper.makeDeploymentSettings(UniversalDocs, packageBin in UniversalDocs, "zip") ++
      SettingsHelper.addPackage(UniversalDocs, packageXzTarball in UniversalDocs, "txz")
}
