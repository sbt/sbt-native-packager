package com.typesafe.packager
package universal

trait UniversalKeys {

}

object Keys extends UniversalKeys {
  def mappings = sbt.Keys.mappings
  def packageBin = sbt.Keys.packageBin
  def name = sbt.Keys.name
  def target = sbt.Keys.target
  def sourceDirectory = sbt.Keys.sourceDirectory
}