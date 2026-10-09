package com.typesafe.sbt.packager

import sbt.{*, given}
import sbtcompat.PluginCompat.*

/** A set of helper methods to simplify the writing of mappings */
object MappingsHelper extends Mapper with MappingHelperCommon {

  /**
    * It lightens the build file if one wants to give a string instead of file.
    *
    * @example
    *   {{{
    * Universal / mappings ++= directory("extra")
    *   }}}
    *
    * @param sourceDir
    * @return
    *   mappings
    */
  def directory(sourceDir: String): Seq[(File, String)] =
    directory(file(sourceDir))

  /**
    * It lightens the build file if one wants to give a string instead of file.
    *
    * @example
    *   {{{
    * Universal / mappings ++= contentOf("extra")
    *   }}}
    *
    * @param sourceDir
    *   as string representation
    * @return
    *   mappings
    */
  def contentOf(sourceDir: String): Seq[(File, String)] =
    contentOf(file(sourceDir))
}
