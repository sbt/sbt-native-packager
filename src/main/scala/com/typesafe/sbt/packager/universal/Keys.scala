package com.typesafe.sbt
package packager
package universal

import sbt._

trait UniversalKeys {
  val packageZipTarball = TaskKey[File]("package-zip-tarball", "Creates a tgz package.")
  val packageXzTarball = TaskKey[File]("package-xz-tarball", "Creates a txz package.")
  val packageOsxDmg = TaskKey[File]("package-osx-dmg", "Creates a dmg package for OSX (only on osx).")
  val stagingDirectory = SettingKey[File]("stagingDirectory", "The location where a staged distribution will be generated.")
  val stage = TaskKey[Unit]("stage", "Create a local directory with all the files laid out as they would be in the final distribution.")
  val dist = TaskKey[File]("dist", "Creates the distribution packages.")
}

object Keys extends UniversalKeys {
  def mappings = sbt.Keys.mappings
  def packageBin = sbt.Keys.packageBin  
  def packageSrc = sbt.Keys.packageSrc
  def packageDoc = sbt.Keys.packageDoc
  def name = sbt.Keys.name
  def target = sbt.Keys.target
  def sourceDirectory = sbt.Keys.sourceDirectory
}