package com.typesafe.sbt.packager.archetypes

import sbt._
import com.typesafe.sbt.packager.archetypes.ServerLoader._

object BashScript {

  val LOADER_FUNCTIONS = "loader-functions"

  /**
   *
   * @param templateName - DebianPlugin.Names for maintainer scripts and "start"
   * @param loader - which startup system
   * @param replacements - default replacements
   * @param template - if specified, it will override the default one
   */
  def apply(
    script: String,
    archetype: String,
    config: Configuration,
    replacements: Seq[(String, String)],
    template: Option[URL] = None): String = {
    // use template or else search for a default
    //  TODO make this an Option
    val url = template getOrElse {
      getClass getResource s"$archetype/${config.name}/$script"
    }

    // if an url was found, create the script
    TemplateWriter generateScript (url, replacements)
  }

  /**
   * Loads the [[ServerLoader]] specific "functions" resource,
   * replaces all placeholders and returns the resolved string.
   *
   * The functions script resides in "[archetype]/[loader]/functions"
   *
   * @param loader - Upstart, SystemV, SystemD
   * @param replacements - tuple of name->replacement
   * @param script - default is "functions"
   * @return functions - addService/stopService with resolved variables
   */
  def loaderFunctionsReplacement(loader: ServerLoader, archetype: String,
    script: String = LOADER_FUNCTIONS): (String, String) = {
    val source = getClass.getResource(templatePath(loader, script, archetype))
    println(source)
    LOADER_FUNCTIONS -> TemplateWriter.generateScript(source, Nil)
  }

  def templatePath(loader: ServerLoader, script: String, archetype: String): String =
    archetype + "/systemloader/" + loader.toString + "/" + script

}
