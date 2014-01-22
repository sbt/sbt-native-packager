package com.typesafe.sbt.packager.archetypes

import java.io.File
import java.net.URL

/**
 * Constructs an start script for running a java application.
 *
 */
object JavaAppStartScript {

  import ServerLoader._
  import com.typesafe.sbt.packager.debian.DebianPlugin.Names._
  val startScript = "start"

  private val upstartScripts = Seq(startScript, Postinst, Prerm)
  private val systemvScripts = Seq(startScript, Postinst, Prerm, Postrm)

  /**
   * Generating the URL to the startScript template.
   * 1. Looking in defaultLocation
   * 2. Using default fallback
   *
   * @param loader - used, when no file in the defaultLocation
   * @param defaultLocation - use if exists
   */
  def defaultStartScriptTemplate(loader: ServerLoader, defaultLocation: File): URL =
    if (defaultLocation.exists) defaultLocation.toURI.toURL
    else templateUrl(startScript, loader) getOrElse sys.error("Default startscript not available for loader: " + loader)

  /**
   * Generating the start script depending on the serverLoader.
   *
   * @param loader - which startup system
   * @param replacements - default replacements
   * @param template - if specified, it will override the default one
   */
  def generateStartScript(
    loader: ServerLoader,
    replacements: Seq[(String, String)],
    template: Option[URL] = None): Option[String] = generateTemplate(startScript, loader, replacements, template)

  /**
   *
   * @param templateName - DebianPlugin.Names for maintainer scripts and "start"
   * @param loader - which startup system
   * @param replacements - default replacements
   * @param template - if specified, it will override the default one
   */
  def generateTemplate(
    templateName: String,
    loader: ServerLoader,
    replacements: Seq[(String, String)],
    template: Option[URL] = None): Option[String] = {

    // use template orElse search for a default
    val url = templateUrl(templateName, loader, template)

    // if an url was found, create the script
    url map { template =>
      TemplateWriter generateScript (template, replacements)
    }
  }

  def templateUrl(templateName: String, loader: ServerLoader, template: Option[URL] = None): Option[URL] = template orElse {
    Option(loader match {
      case Upstart if (upstartScripts contains templateName) =>
        getClass getResource ("upstart/" + templateName + "-template")
      case SystemV if (systemvScripts contains templateName) =>
        getClass getResource ("systemv/" + templateName + "-template")
      case _ => null
    })
  }

  /**
   *
   * @param author -
   * @param description - short description
   * @param execScript - name of the script in /usr/bin
   * @param chdir - execution path of the script
   * @param retries - on fail, how often should a restart be tried
   * @param retryTimeout - pause between retries
   * @return Seq of key,replacement pairs
   */
  def makeReplacements(
    author: String,
    description: String,
    execScript: String,
    chdir: String,
    appName: String,
    appMainClass: String,
    appClasspath: String,
    daemonUser: String,
    retries: Int = 0,
    retryTimeout: Int = 60): Seq[(String, String)] =
    Seq(
      "author" -> author,
      "descr" -> description,
      "exec" -> execScript,
      "chdir" -> chdir,
      "retries" -> retries.toString,
      "retryTimeout" -> retryTimeout.toString,
      "app_name" -> appName,
      "app_main_class" -> appMainClass,
      "app_classpath" -> appClasspath,
      "daemon_user" -> daemonUser)
}

object ServerLoader extends Enumeration {
  type ServerLoader = Value
  val Upstart, SystemV = Value
}
