package com.typesafe.sbt.packager.archetypes.scripts

import com.typesafe.sbt.packager.archetypes.TemplateWriter

import java.io.File

/**
  * == Bash StartScript Plugin ==
  *
  * This plugins creates a start bash script to run an application built with the
  * [[com.typesafe.sbt.packager.archetypes.JavaAppPackaging]].
  */
trait LinuxStartScriptGenerator extends CommonStartScriptGenerator {
  override protected[this] val scriptSuffix: String = ""
  override protected[this] val eol: String = "\n"
  override protected[this] val keySurround: String => String = TemplateWriter.bashFriendlyKeySurround
  override protected[this] val executableBitValue: Boolean = true

  protected[this] case class BashScriptConfig(
    override val executableScriptName: String,
    override val scriptClasspath: Seq[String],
    override val replacements: Seq[(String, String)],
    override val templateLocation: File
  ) extends ScriptConfig {
    override def withScriptName(scriptName: String): BashScriptConfig = copy(executableScriptName = scriptName)
  }

  private[this] def usageMainClassReplacement(mainClasses: Seq[String]): String =
    if (mainClasses.nonEmpty)
      mainClasses.mkString("Available main classes:\n\t", "\n\t", "")
    else
      ""

  protected[this] def generateScriptReplacements(defines: Seq[String]): Seq[(String, String)] = {
    val defineString = defines mkString "\n"
    Seq("template_declares" -> defineString)
  }

  override protected[this] def createReplacementsForMainScript(
    mainClass: String,
    mainClasses: Seq[String],
    config: SpecializedScriptConfig
  ): Seq[(String, String)] =
    Seq(
      "app_mainclass" -> mainClass,
      "available_main_classes" -> usageMainClassReplacement(mainClasses)
    ) ++ config.replacements

  /**
    * The type of specialized ScriptConfig.
    * This enables callback methods of the concrete plugin implementations
    * to use fields of config that only exist in their ScriptConfig specialization.
    */
  override protected[this] type SpecializedScriptConfig = BashScriptConfig
}
