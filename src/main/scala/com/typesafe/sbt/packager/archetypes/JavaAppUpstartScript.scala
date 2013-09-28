package com.typesafe.sbt.packager.archetypes

/**
 * Constructs an upstart script for running a java application.
 *
 * Makes use of the associated upstart-template, with a few hooks
 *
 */
object JavaAppUpstartScript {

  private[this] def upstartTemplateSource: java.net.URL = getClass.getResource("upstart-template")

  private[this] def postinstTemplateSource: java.net.URL = getClass.getResource("postinst-template")
  private[this] def preremTemplateSource: java.net.URL = getClass.getResource("prerem-template")
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
    descr: String,
    execScript: String,
    chdir: String,
    retries: Int = 0,
    retryTimeout: Int = 60): Seq[(String, String)] = Seq(
    "exec" -> execScript,
    "author" -> author,
    "descr" -> descr,
    "chdir" -> chdir,
    "retries" -> retries.toString,
    "retryTimeout" -> retryTimeout.toString)

  def generateScript(replacements: Seq[(String, String)]): String =
    TemplateWriter.generateScript(upstartTemplateSource, replacements)

    
  def generatePrerem(appName: String): String =
    TemplateWriter.generateScript(preremTemplateSource, Seq("app_name" -> appName))
  def generatePostinst(appName: String): String =
    TemplateWriter.generateScript(postinstTemplateSource, Seq("app_name" -> appName))
}
