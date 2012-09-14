package com.typesafe.sbt
package packager
package universal

import sbt._

trait UniversalKeys {
  val packageZipTarball = TaskKey[File]("package-zip-tarball", "Creates a tgz package.")
  val packageXzTarball = TaskKey[File]("package-xz-tarball", "Creates a txz package.")
}

object Keys extends UniversalKeys {
  def mappings = sbt.Keys.mappings
  def packageBin = sbt.Keys.packageBin  
  def name = sbt.Keys.name
  def target = sbt.Keys.target
  def sourceDirectory = sbt.Keys.sourceDirectory
}