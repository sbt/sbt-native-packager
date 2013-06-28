package com.typesafe.sbt
package packager
package universal

import sbt._
import Keys._
import Archives._

/** Defines behavior to construct a 'universal' zip for installation. */
trait UniversalPlugin extends Plugin {
  val Universal = config("universal")
  val UniversalDocs = config("universal-docs")
  val UniversalSrc = config("universal-src")
  
  /** The basic settings for the various packaging types. */ 
  def universalSettings: Seq[Setting[_]] = 
    makePackageSettingsForConfig(Universal) ++
    makePackageSettingsForConfig(UniversalDocs) ++ 
    makePackageSettingsForConfig(UniversalSrc)
  
  /** Creates all package types for a given configuration */
  private[this] def makePackageSettingsForConfig(config: Configuration): Seq[Setting[_]] =
    makePackageSettings(packageBin, config)(makeZip) ++
    makePackageSettings(packageOsxDmg, config)(makeDmg) ++
    makePackageSettings(packageZipTarball, config)(makeTgz) ++
    makePackageSettings(packageXzTarball, config)(makeTxz) ++
    inConfig(config)(Seq(
      mappings <<= sourceDirectory map findSources
    )) ++ Seq(
      sourceDirectory in config <<= sourceDirectory apply (_ / config.name),
      target in config <<= target apply (_ / config.name)
    )
  
  def useNativeZip: Seq[Setting[_]] =
    makePackageSettings(packageBin, Universal)(makeNativeZip) ++
    makePackageSettings(packageBin, UniversalDocs)(makeNativeZip) ++
    makePackageSettings(packageBin, UniversalSrc)(makeNativeZip)
    
    
  private type Packager = (File, String, Seq[(File,String)]) => File
  /** Creates packaging settings for a given package key, configuration + archive type. */
  private[this] def makePackageSettings(packageKey: TaskKey[File], config: Configuration)(packager: Packager): Seq[Setting[_]] =
    inConfig(config)(Seq(
      mappings in packageKey <<= mappings,
      packageKey <<= (target, name, mappings in packageKey) map packager
    ))
    
  /** Finds all sources in a source directory. */
  private[this] def findSources(sourceDir: File): Seq[(File, String)] =
    sourceDir.*** --- sourceDir x relativeTo(sourceDir)

}
