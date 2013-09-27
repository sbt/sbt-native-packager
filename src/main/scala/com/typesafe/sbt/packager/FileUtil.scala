package com.typesafe.sbt
package packager

import java.io.File
import sbt.Process

object chmod {
  def apply(file: File, perms: String): Unit =
    Process(Seq("chmod", perms, file.getAbsolutePath)).! match {
      case 0 => ()
      case n => sys.error("Error running chmod " + perms + " " + file)
    }
  def safe(file: File, perms: String): Unit =
    try apply(file, perms)
    catch {
      case e: RuntimeException => ()
    }
}