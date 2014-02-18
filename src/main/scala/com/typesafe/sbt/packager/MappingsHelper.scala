package com.typesafe.sbt
package packager

import sbt._

/** A set of helper methods to simplify the writing of mappings */
object MappingsHelper {

  /** return a Seq of mappings which effect is to add a whole directory in the generated package */
  def directory(sourceDir: File): Seq[(File, String)] = {
    val parentFile = sourceDir.getParentFile
    if (parentFile != null)
      sourceDir.*** pair relativeTo(sourceDir.getParentFile)
    else
      sourceDir.*** pair basic
  }

  /** It lightens the build file if one wants to give a string instead of file. */
  def directory(sourceDir: String): Seq[(File, String)] = {
    directory(file(sourceDir))
  }

  /** return a Seq of mappings which effect is to add the content of directory in the generated package,
    *  excluding the directory itself */
  def contentOf(sourceDir: File): Seq[(File, String)] = {
    (sourceDir.*** --- sourceDir) pair relativeTo(sourceDir)
  }

  /** It lightens the build file if one wants to give a string instead of file. */
  def contentOf(sourceDir: String): Seq[(File, String)] = {
    contentOf(file(sourceDir))
  }
}

