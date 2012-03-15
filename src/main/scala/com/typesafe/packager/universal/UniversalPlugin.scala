package com.typesafe.packager.universal

import sbt._
import Keys._

/** Defines behavior to construct a 'universal' zip for installation. */
trait UniversalPlugin extends Plugin {
  val Universal = config("universal")
  
  def universalSettings: Seq[Setting[_]] = 
    inConfig(Universal)(Seq(
      mappings <<= sourceDirectory map findSources,
      packageBin <<= (target, name, mappings) map makeZip
    )) ++ Seq(
      sourceDirectory in Universal <<= sourceDirectory apply (_ / "universal"),
      target in Universal <<= target apply (_ / "universal")
    )
  
  private[this] def findSources(sourceDir: File): Seq[(File, String)] =
    sourceDir.*** --- sourceDir x relativeTo(sourceDir)
    
  private[this] def makeZip(target: File, name: String, mappings: Seq[(File, String)]): File = {
    val zip = target / (name + ".zip")
    sbt.IO.zip(mappings, zip)
    zip
  }
}