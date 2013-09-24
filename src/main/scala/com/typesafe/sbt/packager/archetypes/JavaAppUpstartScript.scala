package com.typesafe.sbt.packager.archetypes

/**
 * Constructs an upstart script for running a java application.
 *
 * Makes use of the associated upstart-template, with a few hooks
 *
 */
object JavaAppUpstartScript {

  private[this] def upstartTemplateSource = getClass.getResource("upstart-template")
  private[this] def charset = java.nio.charset.Charset.forName("UTF-8")

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

  private def replace(line: String, replacements: Seq[(String, String)]): String = {
    replacements.foldLeft(line) {
      case (line, (key, value)) =>
        ("\\$\\{\\{" + key + "\\}\\}").r.replaceAllIn(line, java.util.regex.Matcher.quoteReplacement(value))
    }
  }

  def generateScript(replacements: Seq[(String, String)]): String = {
    val sb = new StringBuilder
    for (line <- sbt.IO.readLinesURL(upstartTemplateSource, charset)) {
      sb append replace(line, replacements)
      sb append "\n"
    }
    sb toString
  }

}
