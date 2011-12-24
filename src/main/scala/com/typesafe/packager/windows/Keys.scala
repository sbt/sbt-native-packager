package com.typesafe.packager
package windows

import sbt._

trait WindowsKeys {
  val wixConfig = TaskKey[xml.Node]("wix-xml", "The WIX XML configuration for this package.")
  val wixFile = TaskKey[File]("wix-file", "The WIX XML file to package with.")
  val packageMsi = TaskKey[File]("package-cab", "creates a new windows CAB file containing everything for the installation.")
}

object Keys extends WindowsKeys {
  def target = sbt.Keys.target
  def mappings = sbt.Keys.mappings
  def name = sbt.Keys.name
  def streams = sbt.Keys.streams
  def sourceDirectory = sbt.Keys.sourceDirectory
}