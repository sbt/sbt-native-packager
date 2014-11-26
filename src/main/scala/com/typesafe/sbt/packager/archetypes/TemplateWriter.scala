package com.typesafe.sbt.packager.archetypes

/**
 * This object provides methods to generate scripts from templates. This involves
 *
 * <ol>
 * <li>procesing - replacing a placeholders with actual values</li>
 * <li>TODO: validating - check the script if there are no remaining placeholders</li>
 * </ol>
 *
 * @example a bash script can be generated like this
 * {{{
 *  val template = getClass getResource "template-your-bashscript"
 *  val replacements = Seq("name" -> "your-app", "custom" -> "1")
 *  TemplateWriter.generateScript(template, replacements)
 * }}}
 *
 * @example a bat script can be generated like this
 * {{{
 *  val template = getClass getResource "template-your-batscript"
 *  val replacements = Seq("name" -> "your-app", "custom" -> "1")
 *  TemplateWriter.generateScript(template, replacements, "\r\n", TemplateWriter.batFriendlyKeySurround)
 * }}}
 */
object TemplateWriter {
  def defaultCharset: java.nio.charset.Charset = java.nio.charset.Charset.forName("UTF-8")

  def bashFriendlyKeySurround(key: String) =
    "\\$\\{\\{" + key + "\\}\\}"
  def batFriendlyKeySurround(key: String) =
    "@@" + key + "@@"

  private def replace(
    line: String,
    replacements: Seq[(String, String)],
    keySurround: String => String): String = {
    replacements.foldLeft(line) {
      case (line, (key, value)) =>
        keySurround(key).r.replaceAllIn(line, java.util.regex.Matcher.quoteReplacement(value))
    }
  }

  private def replaceValues(
    lines: Seq[String],
    replacements: Seq[(String, String)],
    eol: String,
    keySurround: String => String): String = {
    val sb = new StringBuilder
    for (line <- lines) {
      sb append replace(line, replacements, keySurround)
      sb append eol
    }
    sb toString
  }

  def generateScript(
    source: java.net.URL,
    replacements: Seq[(String, String)],
    eol: String = "\n",
    keySurround: String => String = bashFriendlyKeySurround,
    charset: java.nio.charset.Charset = defaultCharset): String = {
    val lines = sbt.IO.readLinesURL(source, charset)
    replaceValues(lines, replacements, eol, keySurround)
  }

  def generateScriptFromString(
    source: String,
    replacements: Seq[(String, String)],
    eol: String = "\n",
    keySurround: String => String = bashFriendlyKeySurround,
    charset: java.nio.charset.Charset = defaultCharset): String = {
    replaceValues(source split eol, replacements, eol, keySurround)
  }
}