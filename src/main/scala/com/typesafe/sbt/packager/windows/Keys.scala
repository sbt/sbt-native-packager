package com.typesafe.sbt
package packager
package windows

import sbt._

/** windows settings */
trait WindowsKeys {

  val wixProductId =
    SettingKey[String]("wix-product-id", "The uuid of the windows package.")
  val wixProductUpgradeId =
    SettingKey[String]("wix-product-upgrade-id", "The uuid associated with upgrades for this package.")
  val wixPackageInfo = SettingKey[WindowsProductInfo]("wix-package-info", "The configuration for this package.")
  @transient
  val wixProductLicense = TaskKey[Option[File]]("wix-product-license", "The RTF file to display with licensing.")
  @transient
  val wixFeatures =
    TaskKey[Seq[WindowsFeature]]("wix-features", "Configuration of the windows installable features for this package.")
  @transient
  val wixProductConfig =
    TaskKey[xml.Node]("wix-product-xml", "The WIX XML configuration for a product (nested in Wix/Product elements).")
  @transient
  val wixConfig =
    TaskKey[xml.Node]("wix-xml", "The WIX XML configuration for this package.")
  @deprecated("Use wixFiles task instead", "1.3.15")
  val wixFile = TaskKey[File]("wix-file", "The generated WIX XML file.")
  @transient
  val wixFiles = TaskKey[Seq[File]]("wix-files", "WIX XML sources (*.wxs) to package with")
  val candleOptions = SettingKey[Seq[String]]("candle-options", "Options to pass to the candle.exe program.")
  val lightOptions = SettingKey[Seq[String]]("light-options", "Options to pass to the light.exe program.")
  val wixMajorVersion =
    SettingKey[Int]("wix-major-version", "Major version of the Wix suit.")

}
