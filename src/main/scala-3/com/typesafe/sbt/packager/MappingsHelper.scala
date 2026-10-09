package com.typesafe.sbt.packager

import sbt.{*, given}
import sbt.Keys.fileConverter
import sbt.io.*
import sbtcompat.PluginCompat.*
import xsbti.FileConverter

/** A set of helper methods to simplify the writing of mappings */
object MappingsHelper extends Mapper with MappingHelperCommon {

  /**
    * It lightens the build file if one wants to give a string instead of file.
    *
    * @example
    *   {{{
    * Universal / mappings ++= directory("extra").value
    *   }}}
    *
    * @param sourceDir
    * @return
    *   mappings
    */
  def directory(sourceDir: String): Def.Initialize[Seq[(FileRef, String)]] =
    Def.setting {
      implicit val conv: FileConverter = fileConverter.value
      directory(file(sourceDir)).map { case (f, p) =>
        toFileRef(f) -> p
      }
    }

  /**
    * It lightens the build file if one wants to give a string instead of file.
    *
    * @example
    *   {{{
    * Universal / mappings ++= contentOf("extra").value
    *   }}}
    *
    * @param sourceDir
    *   as string representation
    * @return
    *   mappings
    */
  def contentOf(sourceDir: String): Def.Initialize[Seq[(FileRef, String)]] =
    Def.setting {
      implicit val conv: FileConverter = fileConverter.value
      contentOf(sourceDir = file(sourceDir), conv0 = conv)
    }
}
