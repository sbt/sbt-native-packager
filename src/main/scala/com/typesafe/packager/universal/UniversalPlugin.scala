package com.typesafe.packager.universal

import sbt._
import Keys._

/** Defines behavior to construct a 'universal' zip for installation. */
trait UniversalPlugin extends Plugin {
  val Universal = config("universal")
  
  // TODO - Figure out permissions....
  
  def universalSettings: Seq[Setting[_]] = 
    makePackageSettings(packageBin, Universal)(makeZip) ++
    makePackageSettings(packageZipTarball, Universal)(makeZippedTarball) ++
    inConfig(Universal)(Seq(
      mappings <<= sourceDirectory map findSources,
      mappings in packageBin <<= mappings,
      packageBin <<= (target, name, mappings) map makeZip
    )) ++ Seq(
      sourceDirectory in Universal <<= sourceDirectory apply (_ / "universal"),
      target in Universal <<= target apply (_ / "universal")
    )
  
  type Packager = (File, String, Seq[(File,String)]) => File
    
  private[this] def makePackageSettings(packageKey: TaskKey[File], config: Configuration)(packager: Packager): Seq[Setting[_]] =
    inConfig(config)(Seq(
      mappings in packageKey := Seq.empty,
      packageKey <<= (target, name, mappings in packageKey) map packager
    ))
    
    
  private[this] def findSources(sourceDir: File): Seq[(File, String)] =
    sourceDir.*** --- sourceDir x relativeTo(sourceDir)
    
  private[this] def makeZip(target: File, name: String, mappings: Seq[(File, String)]): File = {
    val zip = target / (name + ".zip")
    sbt.IO.zip(mappings, zip)
    zip
  }
  
  
  // TODO - Re-use this with txz like so:
  /* Process(Seq("xz", "-9e", "-S", ".gz", tmptar.getAbsolutePath), Some(rdir)).! match {
        case 0 => ()
        case n => sys.error("Error xzing " + tarball + ". Exit code: " + n)
      } */
  private[this] def makeZippedTarball(target: File, name: String, mappings: Seq[(File, String)]): File = {
    val relname = name
    val tarball = target / (name + ".tgz")
    IO.withTemporaryDirectory { f =>
      val rdir = f / relname
      val m2 = mappings map { case (f, p) => f -> (rdir / p) }
      IO.copy(m2)
      
      for(f <- (m2 map { case (_, f) => f } ); if f.getAbsolutePath contains "/bin/") {
        println("Making " + f.getAbsolutePath + " executable")
        f.setExecutable(true)
      }
      IO.createDirectory(tarball.getParentFile)      
      val distdir = IO.listFiles(rdir).head
      val tmptar = f / (relname + ".tar")
      Process(Seq("tar", "-pcvf", tmptar.getAbsolutePath, distdir.getName), Some(rdir)).! match {
        case 0 => ()
        case n => sys.error("Error tarballing " + tarball + ". Exit code: " + n)
      }
      Process(Seq("gzip", "-9", tmptar.getAbsolutePath), Some(rdir)).! match {
        case 0 => ()
        case n => sys.error("Error gziping " + tarball + ". Exit code: " + n)
      }
      IO.copyFile(f / (relname + ".tar.gz"), tarball)
    }
    tarball
  }

}