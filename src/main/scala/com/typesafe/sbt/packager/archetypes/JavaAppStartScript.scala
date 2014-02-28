package com.typesafe.sbt.packager.archetypes

import java.io.File
import java.net.URL

/**
 *  Trait for building start scripts.
 */
trait JavaAppStartScriptBuilder {

  import ServerLoader._

  /** Name of the start script template without '-template' suffix */
  val startScript: String

  /** Scripts to include for upstart. By default only startScript */
  val upstartScripts: Seq[String]

  /** Scripts to include for ssystemV. By default only startScript */
  val systemvScripts: Seq[String]

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

  /**
   * @return url to the template if it's defined for the server loader
   */
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
    daemonUser: String,
    daemonGroup: String,
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
      "daemon_user" -> daemonUser,
      "daemon_group" -> daemonGroup)
}

/**
 * Constructs an start script for running a java application.
 * Can build the neccessary maintainer scripts, too.
 */
object JavaAppStartScript {

  object Rpm extends JavaAppStartScriptBuilder {
    val startScript = "start-rpm"
    val upstartScripts = Seq(startScript)
    val systemvScripts = Seq(startScript)
  }

  object Debian extends JavaAppStartScriptBuilder {
    import com.typesafe.sbt.packager.debian.DebianPlugin.Names._

    val startScript = "start-debian"
    val upstartScripts = Seq(startScript, Postinst, Prerm)
    val systemvScripts = Seq(startScript, Postinst, Prerm, Postrm)
  }

}

object ServerLoader extends Enumeration {
  type ServerLoader = Value
  val Upstart, SystemV = Value
}
