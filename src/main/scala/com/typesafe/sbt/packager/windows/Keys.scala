package com.typesafe.sbt
package packager
package windows

import sbt._

trait WindowsKeys {

  val wixProductId = SettingKey[String]("wix-product-id", "The uuid of the windows package.")
  val wixProductUpgradeId = SettingKey[String]("wix-product-upgrade-id", "The uuid associated with upgrades for this package.")
  val wixPackageInfo = SettingKey[WindowsProductInfo]("wix-package-info", "The configuration for this package.")
  val wixProductLicense = TaskKey[Option[File]]("wix-product-license", "The RTF file to display with licensing.")
  val wixFeatures = TaskKey[Seq[WindowsFeature]]("wix-features", "Configuration of the windows installable features for this package.")
  val wixProductConfig = TaskKey[xml.Node]("wix-product-xml", "The WIX XML configuration for a product (nested in Wix/Product elements).")
  val wixConfig = TaskKey[xml.Node]("wix-xml", "The WIX XML configuration for this package.")
  val wixFile = TaskKey[File]("wix-file", "The WIX XML file to package with.")
  @deprecated("use packageBin instead!", "0.7.0")
  val packageMsi = TaskKey[File]("package-msi", "creates a new windows CAB file containing everything for the installation.")
  val generateWinswFiles = TaskKey[(Option[File], Option[File])]("generateWinswFiles", "Creates Winsw files for Windows Services (First file is winsw exe and the second is the xml config)") //This is experimental
  val candleOptions = SettingKey[Seq[String]]("candle-options", "Options to pass to the candle.exe program.")
  val lightOptions = SettingKey[Seq[String]]("light-options", "Options to pass to the light.exe program.")
}

object Keys extends WindowsKeys {
  def version = sbt.Keys.version
  def target = sbt.Keys.target
  def mappings = sbt.Keys.mappings
  def name = sbt.Keys.name
  def packageName = packager.Keys.packageName
  def executableScriptName = packager.Keys.executableScriptName
  def streams = sbt.Keys.streams
  def sourceDirectory = sbt.Keys.sourceDirectory
  def packageBin = sbt.Keys.packageBin
  def maintainer = packager.Keys.maintainer
  def packageSummary = packager.Keys.packageSummary
  def packageDescription = packager.Keys.packageDescription
}