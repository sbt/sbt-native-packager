package com.typesafe.packager.universal

import sbt._
/** Helper methods to package up files into compressed archives. */
object Archives {
  /** Makes a zip file in the given target directory using the given name. */
  def makeZip(target: File, name: String, mappings: Seq[(File, String)]): File = {
    val zip = target / (name + ".zip")
    // TODO - Ensure mapping strings start with name?
    sbt.IO.zip(mappings, zip)
    zip
  }
  
  /** GZips a file.  Returns the new gzipped file.
   * NOTE: This will 'consume' the input file.
   */
  def gzip(f: File): File = {
    val par = f.getParentFile
    Process(Seq("gzip", "-9", f.getAbsolutePath), Some(par)).! match {
      case 0 => ()
      case n => sys.error("Error gziping " + f + ". Exit code: " + n)
    }
    file(f.getAbsolutePath + ".gz")
  }
  
  /** xz compresses a file.  Returns the new xz compressed file.
   * NOTE: This will 'consume' the input file.
   */
  def xz(f: File): File = {
    val par = f.getParentFile
    Process(Seq("xz", "-9e", "-S", ".xz", f.getAbsolutePath), Some(par)).! match {
      case 0 => ()
      case n => sys.error("Error xz-ing " + f + ". Exit code: " + n)
    }
    file(f.getAbsolutePath + ".xz")
  }
  
  val makeTxz = makeTarball(xz, ".txz") _
  val makeTgz = makeTarball(gzip, ".tgz") _
  val makeTar = makeTarball(identity, ".tar") _
  
   
  /** Helper method used to construct tar-related compression functions. */
  def makeTarball(compressor: File => File, ext: String)(target: File, name: String, mappings: Seq[(File, String)]): File = {
    val relname = name
    val tarball = target / (name + ext)
    IO.withTemporaryDirectory { f =>
      val rdir = f / relname
      val m2 = mappings map { case (f, p) => f -> (rdir / p) }
      IO.copy(m2)
      // TODO - Is this enough?
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
      IO.copyFile(compressor(tmptar), tarball)
    }
    tarball
  }
}