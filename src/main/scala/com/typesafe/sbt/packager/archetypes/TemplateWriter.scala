package com.typesafe.sbt.packager.archetypes

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

  private def replaceValues(lines: Seq[String],
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