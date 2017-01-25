package com.typesafe.sbt
package packager
package windows

import sbt._
import sbt.Keys.{mappings, name, normalizedName, packageBin, sourceDirectory, streams, target, version}
import packager.Keys.{maintainer, packageDescription, packageName, packageSummary}
import SbtNativePackager.Universal

/**
  * == Windows Plugin ==
  *
  * This plugin generates ''msi'' packages that can be installed on windows systems.
  *
  * == Configuration ==
  *
  * In order to configure this plugin take a look at the available [[com.typesafe.sbt.packager.windows.WindowsKeys]]
  *
  * == Requirements ==
  *
  * <ul>
  * <li>Windows System</li>
  * <li>Wix Toolset ([[http://wixtoolset.org/]]) installed
  * </ul>
  *
  * @example Enable the plugin in the `build.sbt`
  * {{{
  *    enablePlugins(WindowsPlugin)
  * }}}
  */
object WindowsPlugin extends AutoPlugin {

  object autoImport extends WindowsKeys {
    val Windows: Configuration = config("windows")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = windowsSettings ++ mapGenericFilesToWindows
  override def requires = universal.UniversalPlugin

  override def projectConfigurations: Seq[Configuration] = Seq(Windows)

  /**
    * default windows settings
    */
  def windowsSettings: Seq[Setting[_]] =
    Seq(
      sourceDirectory in Windows := sourceDirectory.value / "windows",
      target in Windows := target.value / "windows",
      // TODO - Should this use normalized name like the linux guys?
      name in Windows := name.value,
      packageName in Windows := packageName.value,
      // Defaults so that our simplified building works
      candleOptions := Seq("-ext", "WixUtilExtension"),
      lightOptions := Seq("-ext", "WixUIExtension", "-ext", "WixUtilExtension", "-cultures:en-us"),
      wixProductId := WixHelper.makeGUID,
      wixProductUpgradeId := WixHelper.makeGUID,
      wixMajorVersion := 3,
      maintainer in Windows := maintainer.value,
      packageSummary in Windows := packageSummary.value,
      packageDescription in Windows := packageDescription.value,
      wixProductLicense := {
        // TODO - document this default.
        val default = (sourceDirectory in Windows).value / "License.rtf"
        if (default.exists) Some(default)
        else None
      },
      wixPackageInfo := WindowsProductInfo(
        id = wixProductId.value,
        title = (packageSummary in Windows).value,
        version = (version in Windows).value,
        maintainer = (maintainer in Windows).value,
        description = (packageDescription in Windows).value,
        upgradeId = wixProductUpgradeId.value,
        comments = "TODO - we need comments." // TODO - allow comments
      ),
      wixFeatures := Seq.empty,
      wixProductConfig := WixHelper.makeWixProductConfig(
        (name in Windows).value,
        wixPackageInfo.value,
        wixFeatures.value,
        wixProductLicense.value
      ),
      wixConfig := WixHelper.makeWixConfig(
        (name in Windows).value,
        wixPackageInfo.value,
        WixHelper.getNameSpaceDefinitions(wixMajorVersion.value),
        wixProductConfig.value
      ),
      wixConfig in Windows := wixConfig.value,
      wixProductConfig in Windows := wixProductConfig.value,
      wixFile := {
        val config = (wixConfig in Windows).value
        val wixConfigFile = (target in Windows).value / ((name in Windows).value + ".wxs")
        IO.write(wixConfigFile, config.toString)
        wixConfigFile
      }
    ) ++ inConfig(Windows)(Seq(packageBin := {
      val wixFileValue = wixFile.value
      val msi = target.value / (name.value + ".msi")
      // First we have to move everything (including the wix file) to our target directory.
      val wix = target.value / (name.value + ".wix")
      if (wixFileValue.getAbsolutePath != wix.getAbsolutePath) IO.copyFile(wixFileValue, wix)
      IO.copy(for ((f, to) <- mappings.value) yield (f, target.value / to))
      // Now compile WIX
      val wixdir = Option(System.getenv("WIX")) getOrElse sys.error(
          "WIX environment not found.  Please ensure WIX is installed on this computer."
        )
      val candleCmd = Seq(wixdir + "\\bin\\candle.exe", wix.getAbsolutePath) ++ candleOptions.value
      streams.value.log.debug(candleCmd mkString " ")
      Process(candleCmd, Some(target.value)) ! streams.value.log match {
        case 0 => ()
        case exitCode => sys.error(s"Unable to run WIX compilation to wixobj. Exited with ${exitCode}")
      }
      // Now create MSI
      val wixobj = target.value / (name.value + ".wixobj")
      val lightCmd = Seq(wixdir + "\\bin\\light.exe", wixobj.getAbsolutePath) ++ lightOptions.value
      streams.value.log.debug(lightCmd mkString " ")
      Process(lightCmd, Some(target.value)) ! streams.value.log match {
        case 0 => ()
        case exitCode => sys.error(s"Unable to run build msi. Exited with ${exitCode}")
      }
      msi
    }))

  /**
    * set the `mappings in Windows` and the `wixFeatures`
    */
  def mapGenericFilesToWindows: Seq[Setting[_]] = Seq(
    mappings in Windows := (mappings in Universal).value,
    wixFeatures := makeWindowsFeatures((packageName in Windows).value, (mappings in Windows).value)
  )

  /**
    * Generates the wix configuration features
    *
    * @param name - title of the core package
    * @param mappings - use to generate different features
    * @return windows features
    */
  def makeWindowsFeatures(name: String, mappings: Seq[(File, String)]): Seq[windows.WindowsFeature] = {
    // TODO select main script!  Filter Config links!

    val files =
      for {
        (file, name) <- mappings
        if !file.isDirectory
      } yield ComponentFile(name, editable = name startsWith "conf")
    val corePackage =
      WindowsFeature(
        id = WixHelper.cleanStringForId(name + "_core").takeRight(38), // Must be no longer
        title = name,
        desc = "All core files.",
        absent = "disallow",
        components = files
      )
    // TODO - Detect bat files to add paths...
    val addBinToPath =
      // TODO - we may have issues here...
      WindowsFeature(
        id = "AddBinToPath",
        title = "Update Environment Variables",
        desc = "Update PATH environment variables (requires restart).",
        components = Seq(AddDirectoryToPath("bin"))
      )
    val configLinks = for {
      (file, name) <- mappings
      if !file.isDirectory
      if name startsWith "conf/"
    } yield name.replaceAll("//", "/").stripSuffix("/").stripSuffix("/")
    val menuLinks =
      WindowsFeature(
        id = "AddConfigLinks",
        title = "Configuration start menu links",
        desc = "Adds start menu shortcuts to edit configuration files.",
        components = Seq(AddShortCuts(configLinks))
      )
    // TODO - Add feature for shortcuts to binary scripts.
    Seq(corePackage, addBinToPath, menuLinks)
  }
}

object WindowsDeployPlugin extends AutoPlugin {

  import WindowsPlugin.autoImport._

  override def requires = WindowsPlugin

  override def projectSettings: Seq[Setting[_]] =
    SettingsHelper.makeDeploymentSettings(Windows, packageBin in Windows, "msi")
}
