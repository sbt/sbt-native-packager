package com.typesafe.sbt.packager.archetypes

import java.net.URL

/**
 * Constructs a bash script for running a java application.
 *
 * Makes use of the associated bash-template, with a few hooks
 *
 */
object JavaAppAshScript {

  private[this] def bashTemplateSource =
    getClass.getResource("bash-template")

  /**
   * Creates the block of defines for a script.
   *
   * @param mainClass The required "main" method class we use to run the program.
   * @param appClasspath A sequence of relative-locations (to the lib/ folder) of jars
   *                     to include on the classpath.
   * @param configFile An (optional) filename from which the script will read arguments.
   * @param extras  Any additional defines/commands that should be run in this script.
   */
  def makeDefines(
    mainClass: String,
    appClasspath: Seq[String] = Seq("*"),
    configFile: Option[String] = None,
    extras: Seq[String] = Nil): Seq[String] =
    Seq(mainClassDefine(mainClass)) ++
      (configFile map configFileDefine).toSeq ++
      Seq(makeClasspathDefine(appClasspath)) ++
      extras

  private def makeClasspathDefine(cp: Seq[String]): String = {
    val fullString = cp map (n => "$lib_dir/" + n) mkString ":"
    "app_classpath=\"" + fullString + "\"\n"
  }
  def generateScript(defines: Seq[String], template: URL = bashTemplateSource): String = {
    val defineString = defines mkString "\n"
    val replacements = Seq("template_declares" -> defineString)
    TemplateWriter.generateScript(template, replacements)
  }

  def configFileDefine(configFile: String) =
    "script_conf_file=\"%s\"" format (configFile)

  def mainClassDefine(mainClass: String) = {
    val jarPrefixed = """^\-jar (.*)""".r
    val args = mainClass match {
      case jarPrefixed(jarName) => Seq("-jar", jarName)
      case className            => Seq(className)
    }
    val quotedArgsSpaceSeparated = args.map(s => "\"" + s + "\"").mkString(" ")
    "app_mainclass=%s\n" format (quotedArgsSpaceSeparated)
  }

}
