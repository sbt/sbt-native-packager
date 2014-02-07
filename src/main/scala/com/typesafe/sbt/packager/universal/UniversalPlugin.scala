package com.typesafe.sbt
package packager
package universal

import sbt._
import Keys._
import Archives._
import sbt.Keys.TaskStreams

/** Defines behavior to construct a 'universal' zip for installation. */
trait UniversalPlugin extends Plugin {
  val Universal = config("universal")
  val UniversalDocs = config("universal-docs")
  val UniversalSrc = config("universal-src")
  
  /** The basic settings for the various packaging types. */ 
  def universalSettings: Seq[Setting[_]] = 
    Seq[Setting[_]](
      // For now, we provide delegates from dist/stage to universal...
      dist <<= dist in Universal,
      stage <<= stage in Universal,
      // TODO - New default to naming, is this right?
      name in Universal <<= (normalizedName, version) apply (_ + "-" + _)
    ) ++
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
      mappings <<= sourceDirectory map findSources,
      dist <<= (packageBin, streams) map printDist,
      stagingDirectory <<= target apply (_ / "stage"),
      stage <<= (stagingDirectory, mappings, streams) map stageFilesAndDirectories
    )) ++ Seq(
      sourceDirectory in config <<= sourceDirectory apply (_ / config.name),
      target in config <<= target apply (_ / config.name)
    )
  
  def useNativeZip: Seq[Setting[_]] =
    makePackageSettings(packageBin, Universal)(makeNativeZip) ++
    makePackageSettings(packageBin, UniversalDocs)(makeNativeZip) ++
    makePackageSettings(packageBin, UniversalSrc)(makeNativeZip)
    
  private[this] def printDist(dist: File, streams: TaskStreams): File = {
    streams.log.info("")
    streams.log.info("Your package is ready in " + dist.getCanonicalPath)
    streams.log.info("")
    dist
  }
    
  private[this] def stageFilesAndDirectories(to: File, mappings: Seq[(File, String)], streams: TaskStreams): Unit = {
    val copies =  mappings collect { case (f,p) => f -> (to / p) }
    val (files, directories) = copies partition {case (f ,p) => f.isFile }
    // copy files
    IO.copy(files)
    //copy directories with there full content
    directories.map {case (f, p) => IO.copyDirectory(f, p, overwrite = true)}
    // Now set scripts to executable using Java's lack of understanding of permissions.
    // TODO - Config file user-readable permissions....
    for {
      (from, to) <- files
      if from.canExecute
    } {
      to.setExecutable(true)
    }
  }
    
  private type Packager = (File, String, Seq[(File,String)]) => File
  /** Creates packaging settings for a given package key, configuration + archive type. */
  private[this] def makePackageSettings(packageKey: TaskKey[File], config: Configuration)(packager: Packager): Seq[Setting[_]] =
    inConfig(config)(Seq(
      mappings in packageKey <<= mappings map checkMappings,
      packageKey <<= (target, name, mappings in packageKey) map packager
    ))

  /** check that all mapped files actually exist */
  private[this] def checkMappings(mappings: Seq[(File,String)]) : Seq[(File,String)] = {
    mappings collect { case (f, p) => if(f.exists) (f, p) else sys.error("Mapped file " + f + " does not exist.") }
  }

  /** Finds all sources in a source directory. */
  private[this] def findSources(sourceDir: File): Seq[(File, String)] =
    sourceDir.*** --- sourceDir x relativeTo(sourceDir)

}
