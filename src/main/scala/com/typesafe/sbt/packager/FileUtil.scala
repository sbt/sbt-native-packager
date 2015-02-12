package com.typesafe.sbt
package packager

import java.io.{ File, IOException }
import java.nio.file.{ Paths, Files }
import java.nio.file.attribute.{ PosixFilePermission, PosixFilePermissions }

/**
 * Setting the file permissions
 */
object chmod {

  /**
   * Using java 7 nio API to set the permissions.
   *
   * @param file
   * @param perms in octal format
   */
  def apply(file: File, perms: String): Unit =
    try {
      Files.setPosixFilePermissions(file.toPath, permissions(perms))
    } catch {
      case e: IOException => sys.error("Error setting permissions " + perms + " on " + file.getAbsolutePath + ": " + e.getMessage)
    }

}

/**
 * Converts a octal unix permission representation into
 * a java `PosiFilePermissions` compatible string.
 */
object permissions {

  /**
   * @param perms in octal format
   * @return java 7 posix file permissions
   */
  def apply(perms: String): java.util.Set[PosixFilePermission] = PosixFilePermissions fromString convert(perms)

  def convert(perms: String): String = {
    require(perms.length == 4 || perms.length == 3, s"Permissions must have 3 or 4 digits, got [$perms]")
    // ignore setuid/setguid/sticky bit
    val i = if (perms.length == 3) 0 else 1
    val user = Character getNumericValue (perms charAt i)
    val group = Character getNumericValue (perms charAt i + 1)
    val other = Character getNumericValue (perms charAt i + 2)

    asString(user) + asString(group) + asString(other)
  }

  private def asString(perm: Int): String = perm match {
    case 0 => "---"
    case 1 => "--x"
    case 2 => "-w-"
    case 3 => "-wx"
    case 4 => "r--"
    case 5 => "r-x"
    case 6 => "rw-"
    case 7 => "rwx"
  }
}