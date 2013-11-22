package com.typesafe.sbt.packager.archetypes

/**
 * Constructs an start script for running a java application.
 *
 */
object JavaAppStartScript {

  import ServerLoader._

  protected def upstartTemplateSource: java.net.URL = getClass.getResource("upstart-template")
  protected def sysvinitTemplateSource: java.net.URL = getClass.getResource("sysvinit-template")

  protected def postinstTemplateSource: java.net.URL = getClass.getResource("postinst-template")
  protected def preremTemplateSource: java.net.URL = getClass.getResource("prerem-template")


  def generateScript(replacements: Seq[(String, String)], loader: ServerLoader): String =
    loader match {
      case Upstart =>
        TemplateWriter.generateScript(upstartTemplateSource, replacements)
      case SystemV =>
        TemplateWriter.generateScript(sysvinitTemplateSource, replacements)
    }


  def generatePrerm(appName: String): String =
    TemplateWriter.generateScript(preremTemplateSource, Seq("app_name" -> appName))


  def generatePostinst(appName: String): String =
    TemplateWriter.generateScript(postinstTemplateSource, Seq("app_name" -> appName))


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
