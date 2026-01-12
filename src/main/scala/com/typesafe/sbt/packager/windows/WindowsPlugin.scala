package com.typesafe.sbt.packager.windows

import sbt.{*, given}
import sbt.Keys.{fileConverter, mappings, name, packageBin, sourceDirectory, streams, target, version}
import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.Keys.{maintainer, packageDescription, packageName, packageSummary}
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.Compat.*
import com.typesafe.sbt.packager.PluginCompat
import com.typesafe.sbt.packager.SettingsHelper

import com.typesafe.sbt.packager.sourceDateEpoch
import xsbti.FileConverter

/**
  * ==Windows Plugin==
  *
  * This plugin generates ''msi'' packages that can be installed on windows systems.
  *
  * ==Configuration==
  *
  * In order to configure this plugin take a look at the available [[com.typesafe.sbt.packager.windows.WindowsKeys]]
  *
  * ==Requirements==
  *
  *   - Windows System
  *   - Wix Toolset ([[http://wixtoolset.org/]]) installed
  *
  * @example
  *   Enable the plugin in the `build.sbt`
  *   {{{
  *    enablePlugins(WindowsPlugin)
  *   }}}
  */
object WindowsPlugin extends AutoPlugin {

  object autoImport extends WindowsKeys {
    val Windows: Configuration = config("windows")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[?]] = windowsSettings ++ mapGenericFilesToWindows
  override def requires = UniversalPlugin

  override def projectConfigurations: Seq[Configuration] = Seq(Windows)

  /**
    * default windows settings
    */
  def windowsSettings: Seq[Setting[?]] =
    Seq(
      Windows / sourceDirectory := sourceDirectory.value / "windows",
      Windows / target := target.value / "windows",
      // TODO - Should this use normalized name like the linux guys?
      Windows / name := name.value,
      Windows / packageName := packageName.value,
      // Defaults so that our simplified building works
      candleOptions := Seq("-ext", "WixUtilExtension"),
      lightOptions := Seq("-ext", "WixUIExtension", "-ext", "WixUtilExtension", "-cultures:en-us"),
      wixProductId := WixHelper.makeGUID((Windows / packageName).value + "_wixProductId"),
      wixProductUpgradeId := WixHelper.makeGUID((Windows / packageName).value + "_wixProductUpgradeId"),
      wixMajorVersion := 3,
      Windows / maintainer := maintainer.value,
      Windows / packageSummary := packageSummary.value,
      Windows / packageDescription := packageDescription.value,
      wixProductLicense := {
        // TODO - document this default.
        val default = (Windows / sourceDirectory).value / "License.rtf"
        if (default.exists) Some(default)
        else None
      },
      wixPackageInfo := WindowsProductInfo(
        id = wixProductId.value,
        title = (Windows / packageSummary).value,
        version = (Windows / version).value,
        maintainer = (Windows / maintainer).value,
        description = (Windows / packageDescription).value,
        upgradeId = wixProductUpgradeId.value,
        comments = "TODO - we need comments." // TODO - allow comments
      ),
      wixFeatures := Seq.empty,
      wixProductConfig := WixHelper
        .makeWixProductConfig((Windows / name).value, wixPackageInfo.value, wixFeatures.value, wixProductLicense.value),
      wixConfig := WixHelper.makeWixConfig(
        (Windows / name).value,
        wixPackageInfo.value,
        WixHelper.getNameSpaceDefinitions(wixMajorVersion.value),
        wixProductConfig.value
      ),
      Windows / wixConfig := wixConfig.value,
      Windows / wixProductConfig := wixProductConfig.value,
      wixFile := {
        val config = (Windows / wixConfig).value
        val wixConfigFile = (Windows / target).value / ((Windows / name).value + ".wxs")
        IO.write(wixConfigFile, config.toString)
        wixConfigFile
      },
      wixFiles := Seq(wixFile.value)
    ) ++ inConfig(Windows)(Seq(packageBin := Def.uncached {
      val conv0 = fileConverter.value
      implicit val conv: FileConverter = conv0
      val wsxSources = wixFiles.value
      val msi = target.value / (name.value + ".msi")

      // First we have to move everything (including the WIX scripts)
      // to our target directory.
      val targetFlat: Path.FileMap = Path.flat(target.value)
      val wsxFiles = wsxSources.map(targetFlat(_).get)
      val wsxCopyPairs = wsxSources.zip(wsxFiles).filter { case (src, dest) =>
        src.getAbsolutePath != dest.getAbsolutePath
      }
      IO.copy(wsxCopyPairs)
      IO.copy(for ((f, to) <- mappings.value) yield (PluginCompat.toFile(f), target.value / to))

      // Now compile WIX
      val candleCmd = findWixExecutable("candle") +:
        wsxFiles.map(_.getAbsolutePath) ++:
        candleOptions.value
      val wixobjFiles = wsxFiles.map { wsx =>
        wsx.getParentFile / (wsx.base + ".wixobj")
      }

      sourceDateEpoch(target.value)

      streams.value.log.debug(candleCmd mkString " ")
      sys.process.Process(candleCmd, Some(target.value)) ! streams.value.log match {
        case 0        => ()
        case exitCode => sys.error(s"Unable to run WIX compilation to wixobj. Exited with ${exitCode}")
      }

      sourceDateEpoch(target.value)

      // Now create MSI
      val lightCmd = List(findWixExecutable("light"), "-out", msi.getAbsolutePath) ++ wixobjFiles
        .map(_.getAbsolutePath) ++
        lightOptions.value

      streams.value.log.debug(lightCmd mkString " ")
      sys.process.Process(lightCmd, Some(target.value)) ! streams.value.log match {
        case 0        => ()
        case exitCode => sys.error(s"Unable to run build msi. Exited with ${exitCode}")
      }
      PluginCompat.toFileRef(msi)
    }))

  /**
    * set the `Windows / mappings` and the `wixFeatures`
    */
  def mapGenericFilesToWindows: Seq[Setting[?]] =
    Seq(
      Windows / mappings := (Universal / mappings).value,
      wixFeatures := {
        val conv0 = fileConverter.value
        implicit val conv: FileConverter = conv0
        makeWindowsFeatures((Windows / packageName).value, (Windows / mappings).value)
      }
    )

  /**
    * Generates the wix configuration features
    *
    * @param name
    *   title of the core package
    * @param mappings
    *   use to generate different features
    * @return
    *   windows features
    */
  def makeWindowsFeatures(name: String, mappings: Seq[(PluginCompat.FileRef, String)])(implicit
    conv: FileConverter
  ): Seq[WindowsFeature] = {
    // TODO select main script!  Filter Config links!

    val files =
      for {
        (ref, name) <- mappings
        file = PluginCompat.toFile(ref)
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
      (ref, name) <- mappings
      file = PluginCompat.toFile(ref)
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

  private def findWixExecutable(name: String): String = {
    val wixDir = Option(System.getenv("WIX"))
      .map(file)
      .getOrElse(sys.error("WIX environment not found. Please ensure WIX is installed on this computer."))

    val candidates = List(wixDir / (name + ".exe"), wixDir / "bin" / (name + ".exe"))

    candidates.find(_.exists).getOrElse(sys.error(s"WIX executable $name.exe was not found in $wixDir")).getAbsolutePath
  }
}

object WindowsDeployPlugin extends AutoPlugin {

  import WindowsPlugin.autoImport._

  override def requires = WindowsPlugin

  override def projectSettings: Seq[Setting[?]] =
    SettingsHelper.makeDeploymentSettings(Windows, Windows / packageBin, "msi")
}
