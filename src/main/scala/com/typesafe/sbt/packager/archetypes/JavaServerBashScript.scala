package com.typesafe.sbt.packager.archetypes

import sbt._
import com.typesafe.sbt.packager.archetypes.ServerLoader._

object JavaServerBashScript {

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
    template: Option[URL] = None): Option[String] = {
    // use template or else search for a default
    val url = template orElse {
      Option(getClass getResource s"$archetype/${config.name}/$script-template")
    }
    // if an url was found, create the script
    url map {
      TemplateWriter generateScript (_, replacements)
    }
  }

}

object JavaServerLoaderScript {

  val LOADER_FUNCTIONS = "loader-functions"

  def apply(script: String, archetype: String, loader: ServerLoader, template: Option[File]): URL = {
    template flatMap {
      case file if file.exists => Some(file.toURI.toURL)
      case _                   => Option(getClass getResource templatePath(script, loader, archetype))
    } getOrElse (sys.error(s"Could not find init [$script] for system [$loader] in archetype [$archetype]"))
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
    val source = getClass.getResource(templatePath(script, loader, archetype))
    LOADER_FUNCTIONS -> TemplateWriter.generateScript(source, Nil)
  }

  def templatePath(script: String, loader: ServerLoader, archetype: String): String =
    archetype + "/systemloader/" + loader.toString + "/" + script
}
