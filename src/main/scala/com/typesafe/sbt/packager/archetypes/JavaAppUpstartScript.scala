package com.typesafe.sbt.packager.archetypes

import java.io.File
import java.net.URL

/**
 * Constructs an start script for running a java application.
 *
 */
object JavaAppStartScript {

  import ServerLoader._

  protected def upstartTemplateSource: URL = getClass.getResource("upstart-template")
  protected def sysvinitTemplateSource: URL = getClass.getResource("sysvinit-template")
  protected def postinstTemplateSource: URL = getClass.getResource("postinst-template")
  protected def postinstSysvinitTemplateSource: URL = getClass.getResource("postinst-sysvinit-template")
  protected def postrmSysvinitTemplateSource: URL = getClass.getResource("postrm-sysvinit-template")
  protected def preremTemplateSource: URL = getClass.getResource("prerem-template")
  
  
  def defaultStartScriptTemplate(loader: ServerLoader, defaultLocation: File): URL =
    if(defaultLocation.exists) defaultLocation.toURI.toURL
    else loader match {
      case Upstart => upstartTemplateSource
      case SystemV => sysvinitTemplateSource
    }


  def generatePrerm(appName: String, template: java.net.URL = preremTemplateSource): String =
    TemplateWriter.generateScript(template, Seq("app_name" -> appName))


  def generatePostrm(appName: String, loader: ServerLoader, template: Option[java.net.URL] = None): Option[String] =
    (template, loader) match {
      case (Some(template), _) => Option(TemplateWriter.generateScript(template, Seq("app_name" -> appName)))
      case (_, SystemV) =>
        Option(TemplateWriter.generateScript(postrmSysvinitTemplateSource, Seq("app_name" -> appName)))
      case (_, _) => None
    }


  def generatePostinst(appName: String, loader: ServerLoader, template: Option[java.net.URL] = None): String =
    (template, loader) match {
      // User has overriden the default.
      case (Some(template), _) => TemplateWriter.generateScript(template, Seq("app_name" -> appName)) 
      case (_, Upstart) =>
        TemplateWriter.generateScript(postinstTemplateSource, Seq("app_name" -> appName))
      case (_, SystemV) =>
        TemplateWriter.generateScript(postinstSysvinitTemplateSource, Seq("app_name" -> appName))
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
      "daemon_user" -> daemonUser
    )
}


object ServerLoader extends Enumeration {
  type ServerLoader = Value
  val Upstart, SystemV = Value
}
