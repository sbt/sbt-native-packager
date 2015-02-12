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
import java.nio.file.{ Paths, Files, FileSystems, FileSystem, StandardCopyOption }
import java.nio.file.attribute.{ PosixFilePermission, PosixFilePermissions }
import java.net.URI
import scala.collection.JavaConverters._

/**
 *
 *
 *
 * @see http://stackoverflow.com/questions/17888365/file-permissions-are-not-being-preserved-while-after-zip
 * @see http://stackoverflow.com/questions/3450250/is-it-possible-to-create-a-script-to-save-and-restore-permissions
 * @see http://stackoverflow.com/questions/1050560/maintain-file-permissions-when-extracting-from-a-zip-file-using-jdk-5-api
 * @see http://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html
 */
object ZipHelper {
  case class FileMapping(file: File, name: String)

  /**
   * Creates a zip file attempting to give files the appropriate unix permissions using Java 6 APIs.
   * @param sources   The files to include in the zip file.
   * @param outputZip The location of the output file.
   */
  def zipNative(sources: Traversable[(File, String)], outputZip: File): Unit =
    IO.withTemporaryDirectory { dir =>
      val name = outputZip.getName
      val zipDir = dir / (if (name endsWith ".zip") name dropRight 4 else name)
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

      IO.copyFile(zipDir / name, outputZip)
    }

  /**
   * Creates a zip file attempting to give files the appropriate unix permissions using Java 7 APIs.
   *
   * Note: This is known to have some odd issues on MacOSX whereby executable permissions
   * are not actually discovered, even though the Info-Zip headers exist and work on
   * many variants of linux.  Yay Apple.
   *
   * @param sources   The files to include in the zip file.
   * @param outputZip The location of the output file.
   */
  def zip(sources: Traversable[(File, String)], outputZip: File): Unit = {
    val mappings = sources.toSeq.map {
      case (file, name) => FileMapping(file, name)
    }
    archive(mappings, outputZip)
  }

  /**
   * Replaces windows backslash file separator with a forward slash, this ensures the zip file entry is correct for
   * any system it is extracted on.
   * @param path  The path of the file in the zip file
   */
  private def normalizePath(path: String) = {
    val sep = java.io.File.separatorChar
    if (sep == '/')
      path
    else
      path.replace(sep, '/')
  }

  /**
   *
   */
  private def archive(sources: Seq[FileMapping], outputFile: File): Unit = {
    require(!outputFile.isDirectory, "Specified output file " + outputFile + " is a directory.")

    // make sure everything is available
    val outputDir = outputFile.getParentFile
    IO createDirectory outputDir

    // zipping the sources into the output zip
    withZipFilesystem(outputFile) { system =>
      sources foreach {
        case FileMapping(dir, name) if dir.isDirectory => Files createDirectories (system getPath name)
        case FileMapping(file, name)                   => Files copy (file.toPath, system getPath name, StandardCopyOption.COPY_ATTRIBUTES)
      }
    }

  }

  /**
   * Opens a zip filesystem and creates the file if neccessary
   *
   * @param zipFile
   * @param f: FileSystem => Unit, logic working in the filesystem
   */
  def withZipFilesystem(zipFile: File)(f: FileSystem => Unit) {
    val env = Map("create" -> "true").asJava
    val uri = URI.create("jar:file:" + zipFile.getAbsolutePath)

    val system = FileSystems.newFileSystem(uri, env)
    try {
      f(system)
    } finally {
      system.close()
    }
  }
}
