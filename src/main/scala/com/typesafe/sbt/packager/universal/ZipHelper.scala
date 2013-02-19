package com.typesafe.sbt
package packager
package universal

import sbt._
import org.apache.commons.compress.archivers.zip._
import org.apache.commons.compress.compressors.{
  CompressorStreamFactory,
  CompressorOutputStream
}
import java.util.zip.Deflater
import org.apache.commons.compress.utils.IOUtils

object ZipHelper {
  case class FileMapping(file: File, name: String, unixMode: Option[Int] = None)

  /** Creates a zip file attempting to give files the appropriate unix permissions using Java 6 APIs.
   * @param sources   The files to include in the zip file.
   * @param outputZip The location of the output file.
   */
  def zipNative(sources: Traversable[(File,String)], outputZip: File): Unit = 
    IO.withTemporaryDirectory { dir =>
      val name = outputZip.getName
      val zipDir = dir / (if(name endsWith ".zip") name dropRight 4 else name)
      val files = for {
        (file, name) <- sources
      } yield file -> (zipDir / name)
      IO.copy(files)
      for {
        (src, target) <- files
        if src.canExecute
      } target.setExecutable(true, true)
      val dirFileNames = Option(zipDir.listFiles) getOrElse Array.empty[java.io.File] map (_.getName)
      Process(Seq("zip", "-r", name) ++ dirFileNames, zipDir).! match {
        case 0 => ()
        case n => sys.error("Failed to run native zip application!")
      }
    }
  
  /** Creates a zip file attempting to give files the appropriate unix permissions using Java 6 APIs.
   * @param sources   The files to include in the zip file.
   * @param outputZip The location of the output file.
   */
  def zip(sources: Traversable[(File,String)], outputZip: File): Unit = {
    val mappings = 
      for {
        (file, name) <- sources.toSeq
        // TODO - Figure out if this is good enough....
        perm = if(file.isDirectory || file.canExecute) 0755 else 0644 
      } yield FileMapping(file, name, Some(perm))
    archive(mappings, outputZip)
  }
  
  /** Creates a zip file using the given set of filters
   * @param sources   The files to include in the zip file.  A File, Location, Permission pairing.
   * @param outputZip The location of the output file.
   */
  def zipWithPerms(sources: Traversable[(File,String, Int)], outputZip: File): Unit = {
    val mappings = 
      for {
        (file, name, perm) <- sources
      } yield FileMapping(file, name, Some(perm))
    archive(mappings.toSeq, outputZip)
  }
  
    
  private def archive(sources: Seq[FileMapping], outputFile: File): Unit = {
    if(outputFile.isDirectory) sys.error("Specified output file " + outputFile + " is a directory.")
    else {
      val outputDir = outputFile.getParentFile
      IO createDirectory outputDir
      withZipOutput(outputFile) { output =>
        for(FileMapping(file, name, mode) <- sources; if !file.isDirectory) {
          val entry = new ZipArchiveEntry(file, name)
          // Now check to see if we have permissions for this sucker.
          mode foreach (entry.setUnixMode)
          output putArchiveEntry entry
          // TODO - Write file into output?
          IOUtils.copy(new java.io.FileInputStream(file), output)
          output.closeArchiveEntry()
        }
      }
    }
  }
  
  
  private def withZipOutput(file: File)(f: ZipArchiveOutputStream => Unit): Unit = {
    val zipOut = new ZipArchiveOutputStream(file)  
    zipOut setLevel Deflater.BEST_COMPRESSION
    try { f(zipOut) }
    finally {
      zipOut.close() 
    }
  }
}