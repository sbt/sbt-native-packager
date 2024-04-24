package com.typesafe.sbt
package packager
import sbt._

import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.{PosixFilePermission, PosixFilePermissions}

import scala.util.Try

/**
  * Setting the file permissions
  */
object chmod {

  /**
    * Using java 7 nio API to set the permissions.
    *
    * @param file
    * @param perms
    *   in octal format
    */
  def apply(file: File, perms: String): Unit = {
    val posix = permissions(perms)
    val result = Try {
      Files.setPosixFilePermissions(file.toPath, posix)
    } recoverWith {
      // in case of windows
      case e: UnsupportedOperationException =>
        Try {
          file.setExecutable(perms contains PosixFilePermission.OWNER_EXECUTE)
          file.setWritable(perms contains PosixFilePermission.OWNER_WRITE)
        }
    }

    // propagate error
    if (result.isFailure) {
      val e = result.failed.get
      sys.error("Error setting permissions " + perms + " on " + file.getAbsolutePath + ": " + e.getMessage)
    }
  }
}

/**
  * Converts a octal unix permission representation into a java `PosiFilePermissions` compatible string.
  */
object permissions {

  /**
    * @param perms
    *   in octal format
    * @return
    *   java 7 posix file permissions
    */
  def apply(perms: String): java.util.Set[PosixFilePermission] =
    PosixFilePermissions fromString convert(perms)

  def convert(perms: String): String = {
    require(perms.length == 4 || perms.length == 3, s"Permissions must have 3 or 4 digits, got [$perms]")
    // ignore setuid/setguid/sticky bit
    val i = if (perms.length == 3) 0 else 1
    val user = Character getNumericValue (perms charAt i)
    val group = Character getNumericValue (perms charAt i + 1)
    val other = Character getNumericValue (perms charAt i + 2)

    asString(user) + asString(group) + asString(other)
  }

  private def asString(perm: Int): String =
    perm match {
      case 0 => "---"
      case 1 => "--x"
      case 2 => "-w-"
      case 3 => "-wx"
      case 4 => "r--"
      case 5 => "r-x"
      case 6 => "rw-"
      case 7 => "rwx"
    }

  /** Enriches string with `oct` interpolator, parsing string as base 8 integer. */
  implicit class OctalString(val sc: StringContext) extends AnyVal {
    def oct(args: Any*) = Integer.parseInt(sc.s(args: _*), 8)
  }

}

object sourceDateEpoch {

  /**
    * If the SOURCE_DATE_EPOCH environment variable is defined, change the mtime of the file (and all children
    * recursively) to the epoch value. This is useful when trying to create reproducible builds since some packaging
    * tools (e.g. tar, gzip) embed last modified times in the package. If the environment variable is not defined, this
    * does nothing.
    */
  def apply(file: File): Unit =
    sys.env.get("SOURCE_DATE_EPOCH").foreach { epoch =>
      val millis = epoch.toLong
      val par = file.getParentFile
      val allPaths = file.allPaths.get().map(_.getAbsolutePath)
      sys.process.Process(Seq("touch", "-h", "-m", s"--date=@$epoch") ++ allPaths, Some(par)).! match {
        case 0 => ()
        case n => sys.error("Error setting SOURCE_DATE_EPOCH on " + file + ". Exit code: " + n)
      }
    }
}
