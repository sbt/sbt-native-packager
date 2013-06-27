package com.typesafe.sbt
package packager
package windows

import sbt._

trait WindowsKeys {
  val wixConfig = TaskKey[xml.Node]("wix-xml", "The WIX XML configuration for this package.")
  val wixFile = TaskKey[File]("wix-file", "The WIX XML file to package with.")
  @deprecated("use packageBin instead!")
  val packageMsi = TaskKey[File]("package-msi", "creates a new windows CAB file containing everything for the installation.")
  val candleOptions = SettingKey[Seq[String]]("candle-options", "Options to pass to the candle.exe program.")
  val lightOptions = SettingKey[Seq[String]]("light-options", "Options to pass to the light.exe program.")
}

object Keys extends WindowsKeys {
  def target = sbt.Keys.target
  def mappings = sbt.Keys.mappings
  def name = sbt.Keys.name
  def streams = sbt.Keys.streams
  def sourceDirectory = sbt.Keys.sourceDirectory
  def packageBin = sbt.Keys.packageBin
}