package com.typesafe.sbt.packager.archetypes

import sbt._
import com.typesafe.sbt.SbtNativePackager.autoImport.maintainerScripts

/**
  * ==Maintainer Script Helper==
  *
  * Provides utility methods to configure package maintainerScripts.
  */
trait MaintainerScriptHelper {

  /**
    * Use this method to override preexisting configurations with custom file definitions.
    *
    * @example
    *   {{{
    * import DebianConstants._
    * maintainerScripts in Debian := maintainerScriptsFromDirectory(
    *   sourceDirectory.value / DebianSource / DebianMaintainerScripts, Seq(Preinst, Postinst, Prerm, Postrm)
    * )
    *   }}}
    * @param dir
    *   from where to load files
    * @param scripts
    *   a list of script names that should be used
    * @return
    *   filename to content mapping
    */
  def maintainerScriptsFromDirectory(dir: File, scripts: Seq[String]): Map[String, Seq[String]] =
    scripts
      .map(dir / _)
      .filter(_.exists)
      .map { script =>
        script.getName -> IO.readLines(script)
      }
      .toMap

  /**
    * Use this method to append additional script content to specific maintainer scripts.
    *
    * @example
    *   Adding content from a string
    *   {{{
    * import RpmConstants._
    * maintainerScripts in Rpm := maintainerScriptsAppend((maintainerScripts in Rpm).value)(
    *    Pretrans -> "echo 'hello, world'",
    *    Post -> "echo 'installing " + (packageName in Rpm).value + "'"
    * )
    *   }}}
    *
    * @example
    *   Adding content from a string and use script replacements
    *   {{{
    * import DebianConstants._
    * maintainerScripts in Rpm := maintainerScriptsAppend(
    *   (maintainerScripts in Debian).value,
    *   (linuxScriptReplacements in Debian).value
    * )(
    *    Preinst -> "echo 'hello, world'",
    *    Postinst -> s"echo 'installing ${(packageName in Debian).value}'"
    * )
    *   }}}
    *
    * @param current
    *   maintainer scripts
    * @param replacements
    *   (e.g. (linuxScriptReplacements in Debian).value)
    * @param scripts
    *   scriptName -> scriptContent pairs
    * @return
    *   maintainerScripts with appended `scripts`
    * @see
    *   [[maintainerScriptsAppendFromFile]]
    */
  def maintainerScriptsAppend(current: Map[String, Seq[String]] = Map.empty, replacements: Seq[(String, String)] = Nil)(
    scripts: (String, String)*
  ): Map[String, Seq[String]] = {
    val appended = scripts.map { case (key, script) =>
      key -> TemplateWriter.generateScriptFromLines((current.getOrElse(key, Seq.empty) :+ script), replacements)
    }.toMap
    current ++ appended
  }

  /**
    * Use this method to append additional script content to specific maintainer scripts. Note that you won't have any
    * scriptReplacements available.
    *
    * @example
    *   Adding content from a string
    *   {{{
    * import RpmConstants._
    * maintainerScripts in Rpm := maintainerScriptsAppendFromFile((maintainerScripts in Rpm).value)(
    *    Pretrans -> (sourceDirectory.value / "rpm" / "pretrans"),
    *    Post -> (sourceDirectory.value / "rpm" / "posttrans")
    * )
    *   }}}
    *
    * @param current
    *   maintainer scripts
    * @param scripts
    *   scriptName -> scriptFile pairs
    * @return
    *   maintainerScripts with appended `scripts`
    * @see
    *   [[maintainerScriptsAppend]] for pure strings where you can insert arbitrary settings and tasks values
    */
  def maintainerScriptsAppendFromFile(
    current: Map[String, Seq[String]] = Map.empty
  )(scripts: (String, File)*): Map[String, Seq[String]] = {
    val appended = scripts.map {
      case (key, script) if script.exists && script.isFile =>
        key -> (current.getOrElse(key, Seq.empty) ++ IO.readLines(script))
      case (key, script) =>
        sys.error(s"The maintainer script $key doesn't exist here: ${script.getAbsolutePath}")
    }.toMap
    current ++ appended
  }

}

object MaintainerScriptHelper extends MaintainerScriptHelper
