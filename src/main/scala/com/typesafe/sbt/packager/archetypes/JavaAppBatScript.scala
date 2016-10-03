package com.typesafe.sbt.packager.archetypes

object JavaAppBatScript {
  private[this] def bashTemplateSource =
    getClass.getResource("bat-template")
  private[this] def charset =
    java.nio.charset.Charset.forName("UTF-8")

  def makeEnvFriendlyName(name: String): String =
    name.toUpperCase.replaceAll("\\W", "_")

  def makeWindowsRelativeClasspathDefine(cp: Seq[String]): String = {
    def cleanPath(path: String): String = path.replaceAll("/", "\\\\")
    def isAbsolute(path: String): Boolean =
      path.length > 3 && // check path len is long enough to hold a windows absolute path ("c:\ ...")
        Character.isLetter(path(0)) &&
        path(1) == ':'

    def makeRelativePath(path: String): String =
      "%APP_LIB_DIR%\\" + cleanPath(path)
    "set \"APP_CLASSPATH=" + (cp map { path =>
      if (isAbsolute(path)) path else makeRelativePath(path)
    } mkString ";") + "\""
  }

  def makeMainClassDefine(mainClass: String) = {
    "set \"APP_MAIN_CLASS=" + mainClass + "\""
  }

  def makeDefines(defines: Seq[String],
                  replacements: Seq[(String, String)]): String = {
    defines.map(replace(_, replacements)).mkString("\r\n")
  }

  def makeReplacements(
      name: String,
      mainClass: String,
      appClasspath: Seq[String] = Seq("*"),
      extras: Seq[String] = Nil
  ): Seq[(String, String)] = {
    val replacements = Seq(
      "APP_NAME" -> name,
      "APP_ENV_NAME" -> makeEnvFriendlyName(name)
    )
    val defines = Seq(
        makeWindowsRelativeClasspathDefine(appClasspath),
        makeMainClassDefine(mainClass)
      ) ++ extras

    replacements :+ "APP_DEFINES" -> makeDefines(defines, replacements)
  }

  // TODO - use more of the template writer for this...
  def replace(line: String, replacements: Seq[(String, String)]): String = {
    replacements.foldLeft(line) {
      case (line, (key, value)) =>
        line.replaceAll("@@" + key + "@@",
                        java.util.regex.Matcher.quoteReplacement(value))
    }
  }

  def generateScript(
      replacements: Seq[(String, String)],
      template: java.net.URL = bashTemplateSource
  ): String =
    TemplateWriter.generateScript(template,
                                  replacements,
                                  "\r\n",
                                  TemplateWriter.batFriendlyKeySurround)

}
