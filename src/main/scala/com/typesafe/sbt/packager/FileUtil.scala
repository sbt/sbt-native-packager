package com.typesafe.sbt
package packager

import java.io.{IOException, File}
import sbt.Process
import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.{Paths, Path, Files}

object chmod {

  def apply(file: File, perms: String): Unit =
    try {
      Files.setPosixFilePermissions(Paths.get(file.getAbsolutePath), PosixFilePermissions.fromString(perms))
    } catch {
      case e: IOException => sys.error(
        "Error setting permissions " + perms + " on " + file.getAbsolutePath + ": " + e.getMessage)
    }

  def safe(file: File, perms: String): Unit =
    try apply(file, perms)
    catch {
      case e: RuntimeException => ()
    }
}