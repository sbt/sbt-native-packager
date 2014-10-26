package com.typesafe.sbt
package packager
package windows

import sbt._
import sbt.Keys.{ normalizedName, name, version, sourceDirectory, target, mappings, packageBin, streams }
import packager.Keys.{ packageName, maintainer, packageSummary, packageDescription }
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
    val Windows = config("windows")
  }

  import autoImport._

  override lazy val projectSettings = windowsSettings ++ mapGenericFilesToWindows
  override def requires = universal.UniversalPlugin

  /**
   * default windows settings
   */
  def windowsSettings: Seq[Setting[_]] = Seq(
    sourceDirectory in Windows <<= sourceDirectory(_ / "windows"),
    target in Windows <<= target apply (_ / "windows"),
    // TODO - Should this use normalized name like the linux guys?
    name in Windows <<= name,
    packageName in Windows <<= packageName,
    // Defaults so that our simplified building works
    candleOptions := Seq("-ext", "WixUtilExtension"),
    lightOptions := Seq("-ext", "WixUIExtension",
      "-ext", "WixUtilExtension",
      "-cultures:en-us"),
    wixProductId := WixHelper.makeGUID,
    wixProductUpgradeId := WixHelper.makeGUID,
    maintainer in Windows <<= maintainer,
    packageSummary in Windows <<= packageSummary,
    packageDescription in Windows <<= packageDescription,
    wixProductLicense <<= (sourceDirectory in Windows) map { dir =>
      // TODO - document this default.
      val default = dir / "License.rtf"
      if (default.exists) Some(default)
      else None
    },
    wixPackageInfo <<= (
      wixProductId,
      wixProductUpgradeId,
      version in Windows,
      maintainer in Windows,
      packageSummary in Windows,
      packageDescription in Windows) apply { (id, uid, version, mtr, title, desc) =>
        WindowsProductInfo(
          id = id,
          title = title,
          version = version,
          maintainer = mtr,
          description = desc,
          upgradeId = uid,
          comments = "TODO - we need comments." // TODO - allow comments
        )
      },
    wixFeatures := Seq.empty,
    wixProductConfig <<= (name in Windows, wixPackageInfo, wixFeatures, wixProductLicense) map { (name, product, features, license) =>
      WixHelper.makeWixProductConfig(name, product, features, license)
    },
    wixConfig <<= (name in Windows, wixPackageInfo, wixProductConfig) map { (name, product, nested) =>
      WixHelper.makeWixConfig(name, product, nested)
    },
    wixConfig in Windows <<= wixConfig,
    wixProductConfig in Windows <<= wixProductConfig,
    wixFile <<= (wixConfig in Windows, name in Windows, target in Windows) map { (c, n, t) =>
      val f = t / (n + ".wxs")
      IO.write(f, c.toString)
      f
    }
  ) ++ inConfig(Windows)(Seq(
      packageBin <<= (mappings, wixFile, name, target, candleOptions, lightOptions, streams) map { (m, f, n, t, co, lo, s) =>
        val msi = t / (n + ".msi")
        // First we have to move everything (including the wix file) to our target directory.
        val wix = t / (n + ".wix")
        if (f.getAbsolutePath != wix.getAbsolutePath) IO.copyFile(f, wix)
        IO.copy(for ((f, to) <- m) yield (f, t / to))
        // Now compile WIX
        val wixdir = Option(System.getenv("WIX")) getOrElse sys.error("WIX environment not found.  Please ensure WIX is installed on this computer.")
        val candleCmd = Seq(wixdir + "\\bin\\candle.exe", wix.getAbsolutePath) ++ co
        s.log.debug(candleCmd mkString " ")
        Process(candleCmd, Some(t)) ! s.log match {
          case 0 => ()
          case x => sys.error("Unable to run WIX compilation to wixobj...")
        }
        // Now create MSI
        val wixobj = t / (n + ".wixobj")
        val lightCmd = Seq(wixdir + "\\bin\\light.exe", wixobj.getAbsolutePath) ++ lo
        s.log.debug(lightCmd mkString " ")
        Process(lightCmd, Some(t)) ! s.log match {
          case 0 => ()
          case x => sys.error("Unable to run build msi...")
        }
        msi
      }
    ))

  /**
   * set the `mappings in Windows` and the `wixFeatures`
   */
  def mapGenericFilesToWindows: Seq[Setting[_]] = Seq(
    mappings in Windows <<= mappings in Universal,
    wixFeatures <<= (packageName in Windows, mappings in Windows) map makeWindowsFeatures)

  /**
   * Generates the wix configuration features
   * 
   * @param name - title of the core package
   * @param mappings - use to generate different features
   * @return windows features
   */
  def makeWindowsFeatures(name: String, mappings: Seq[(File, String)]): Seq[windows.WindowsFeature] = {
    // TODO select main script!  Filter Config links!
    import windows._

    val files =
      for {
        (file, name) <- mappings
        if !file.isDirectory
      } yield ComponentFile(name, editable = (name startsWith "conf"))
    val corePackage =
      WindowsFeature(
        id = WixHelper.cleanStringForId(name + "_core").takeRight(38), // Must be no longer
        title = name,
        desc = "All core files.",
        absent = "disallow",
        components = files)
    // TODO - Detect bat files to add paths...
    val addBinToPath =
      // TODO - we may have issues here...
      WindowsFeature(
        id = "AddBinToPath",
        title = "Update Enviornment Variables",
        desc = "Update PATH environment variables (requires restart).",
        components = Seq(AddDirectoryToPath("bin")))
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
        components = Seq(AddShortCuts(configLinks)))
    // TODO - Add feature for shortcuts to binary scripts.
    Seq(corePackage, addBinToPath, menuLinks)
  }
}

object WindowsDeployPlugin extends AutoPlugin {

  import WindowsPlugin.autoImport._

  override def requires = WindowsPlugin

  override def projectSettings =
    SettingsHelper.makeDeploymentSettings(Windows, packageBin in Windows, "msi")
}
