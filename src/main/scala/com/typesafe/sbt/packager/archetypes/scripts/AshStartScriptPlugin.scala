package com.typesafe.sbt.packager.archetypes.scripts

import java.io.File

import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.archetypes.{JavaAppPackaging, TemplateWriter}
import sbt.Keys._
import sbt._

 /**
  * == AshStartScript Plugin ==
  *
  * This class is an alternate to JavaAppPackaging designed to support the ash shell.  JavaAppPackaging
  * generates bash-specific code that is not compatible with ash, a very stripped-down, lightweight shell
  * used by popular micro base Docker images like BusyBox.  The AshScriptPlugin will generate simple
  * ash-compatible output.
  *
  * Just like with JavaAppPackaging you can override the bash-template file by creating a src/templates
  * directory and adding your own bash-template file.  Actually this isn't a bad idea as the default
  * bash-template file inherited from JavaAppPackaging has a lot of stuff you probably don't want/need
  * in a highly-constrained environment like ash+BusyBox.  Something much simpler will do, for example:
  *
  * {{{
  * #!/usr/bin/env sh
  *
  * APP_PATH="\u0024(realpath "\u00240")"
  * APP_HOME="\u0024(realpath "\u0024(dirname "\u0024APP_PATH")")"
  * LIB_DIR="\u0024(realpath "\u0024APP_HOME/lib")"
  * CLASSPATH="\u0024LIB_DIR/\u2731"
  * MAINCLASS="\u0024{{mainclass}}"
  *
  * java -classpath "\u0024CLASSPATH" \u0024MAINCLASS \u0024@
  * }}}
  *
  * == Configuration ==
  *
  * This plugin adds new configuration settings to your packaged application.
  * The keys are defined in [[com.typesafe.sbt.packager.archetypes.scripts.AshStartScriptKeys]].
  *
  * Enable this plugin in your `build.sbt` with
  *
  * {{{
  * enablePlugins(AshStartScriptPlugin)
  * }}}
  *
  *
  *
  */
object AshStartScriptPlugin extends AutoPlugin with CommonStartScriptGenerator {

  override protected[this] type SpecializedScriptConfig = AshScriptConfig

  object autoImport extends AshStartScriptKeys

  private[this] object Replacements {

    sealed trait Replacement {
      val templateKey: String
      val value: String
      def replacement: (String, String) = templateKey -> value
    }

    case class Mainclass(override val value: String) extends Replacement {
      override val templateKey: String = "mainclass"
    }

    case class AvailableMainclasses(override val value: String) extends Replacement {
      override val templateKey: String = "available-mainclasses"
    }

    case class Classpath(override val value: String) extends Replacement {
      override val templateKey: String = "classpath"
    }

  }

  protected[this] case class AshScriptConfig(override val executableScriptName: String,
                                             override val scriptClasspath: Seq[String],
                                             override val replacements: Seq[(String, String)],
                                             override val templateLocation: File) extends ScriptConfig {
    override def withScriptName(scriptName: String): AshScriptConfig = copy(executableScriptName = scriptName)
  }

  import Replacements._

  val templateName = "ash-template"

  override def requires: Plugins = JavaAppPackaging

  override protected[this] val scriptSuffix: String = ".sh"
  override protected[this] val forwarderTemplateName: String = "ash-forwarded-template"
  override protected[this] val eol: String = "\n"
  override protected[this] val keySurround: String => String = TemplateWriter.ashFriendlyKeySurround
  override protected[this] val executableBitValue: Boolean = true

  override def projectSettings: Seq[Setting[_]] = Seq(
    makeAshScripts := generateStartScripts(
      AshScriptConfig(
        executableScriptName = executableScriptName.value,
        scriptClasspath = Seq("$LIB_DIR/*"),
        replacements = ashScriptReplacements.value,
        templateLocation = ashScriptTemplateLocation.value
      ),
      (mainClass in Compile).value,
      (discoveredMainClasses in Compile).value,
      (target in Universal).value / "scripts",
      streams.value.log
    ),
    ashScriptTemplateName := templateName,
    ashScriptTemplateLocation := (sourceDirectory.value / "templates" / ashScriptTemplateName.value),
    ashScriptReplacements :=
      Seq(
        Mainclass((mainClass in Compile).value.getOrElse("")).replacement,
        AvailableMainclasses {
          val mainClasses = (discoveredMainClasses in Compile).value
          if (mainClasses.nonEmpty) mainClasses.mkString("Available main classes:\n\t", "\n\t", "") else ""
        }.replacement,
        Classpath("$LIB_DIR/*").replacement
      ),
    mappings in Universal ++= makeAshScripts.value
  )

}
