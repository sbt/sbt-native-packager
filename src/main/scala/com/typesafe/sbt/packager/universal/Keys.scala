package com.typesafe.sbt
package packager
package universal

import sbt._

trait UniversalKeys {
  val packageZipTarball = TaskKey[File]("package-zip-tarball", "Creates a tgz package.")
  val packageXzTarball = TaskKey[File]("package-xz-tarball", "Creates a txz package.")
  val packageOsxDmg = TaskKey[File]("package-osx-dmg", "Creates a dmg package for OSX (only on osx).")
  val stage = TaskKey[Unit]("stage", "Create a local directory with all the files laid out as they would be in the final distribution.")
  val dist = TaskKey[File]("dist", "Creates the distribution packages.")
  val stagingDirectory = SettingKey[File]("stagingDirectory", "Directory where we stage distributions/releases.")
}

object Keys extends UniversalKeys {
  def mappings = sbt.Keys.mappings
  def packageBin = sbt.Keys.packageBin  
  def packageSrc = sbt.Keys.packageSrc
  def packageDoc = sbt.Keys.packageDoc
  def name = sbt.Keys.name
  def normalizedName = sbt.Keys.normalizedName
  def target = sbt.Keys.target
  def sourceDirectory = sbt.Keys.sourceDirectory
  def streams = sbt.Keys.streams
  def version = sbt.Keys.version
}