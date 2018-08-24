package com.typesafe.sbt.packager.archetypes.scripts

import java.io.File

import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.archetypes.{JavaAppPackaging, TemplateWriter}
import com.typesafe.sbt.packager.windows.NameHelper
import sbt.Keys._
import sbt._

/**
  * == Bat StartScript Plugin ==
  *
  * This plugins creates a start bat script to run an application built with the
  * [[com.typesafe.sbt.packager.archetypes.JavaAppPackaging]].
  *
  */
object BatStartScriptPlugin extends AutoPlugin with ApplicationIniGenerator with CommonStartScriptGenerator {

  /**
    * Name of the bat template if user wants to provide custom one
    */
  val batTemplate = "bat-template"

  /**
    * Name of the bat forwarder template if user wants to provide custom one
    */
  override protected[this] val forwarderTemplateName = "bat-forwarder-template"

  /**
    * Location for the application.ini file used by the bat script to load initialization parameters for jvm and app
    */
  val appIniLocation = "%APP_HOME%\\conf\\application.ini"

  override protected[this] val scriptSuffix = ".bat"
  override protected[this] val eol: String = "\r\n"
  override protected[this] val keySurround: String => String = TemplateWriter.batFriendlyKeySurround
  override protected[this] val executableBitValue: Boolean = false

  override val requires = JavaAppPackaging
  override val trigger = AllRequirements

  object autoImport extends BatStartScriptKeys
  import autoImport._

  protected[this] case class BatScriptConfig(override val executableScriptName: String,
                                             override val scriptClasspath: Seq[String],
                                             configLocation: Option[String],
                                             extraDefines: Seq[String],
                                             override val replacements: Seq[(String, String)],
                                             override val templateLocation: File)
      extends ScriptConfig {
    override def withScriptName(scriptName: String): BatScriptConfig = copy(executableScriptName = scriptName)
  }

  override protected[this] type SpecializedScriptConfig = BatScriptConfig

  override def projectSettings: Seq[Setting[_]] = Seq(
    batScriptTemplateLocation := (sourceDirectory.value / "templates" / batTemplate),
    batScriptConfigLocation := (batScriptConfigLocation ?? Some(appIniLocation)).value,
    batScriptExtraDefines := Nil,
    batScriptReplacements := Replacements(executableScriptName.value),
    // Generating the application configuration
    mappings in Universal := generateApplicationIni(
      (mappings in Universal).value,
      (javaOptions in Universal).value,
      batScriptConfigLocation.value,
      (target in Universal).value,
      streams.value.log
    ),
    makeBatScripts := generateStartScripts(
      BatScriptConfig(
        executableScriptName = executableScriptName.value,
        scriptClasspath = (scriptClasspath in batScriptReplacements).value,
        configLocation = batScriptConfigLocation.value,
        extraDefines = batScriptExtraDefines.value,
        replacements = batScriptReplacements.value,
        templateLocation = batScriptTemplateLocation.value
      ),
      (mainClass in (Compile, batScriptReplacements)).value,
      (discoveredMainClasses in Compile).value,
      (target in Universal).value / "scripts",
      streams.value.log
    ),
    mappings in Universal ++= makeBatScripts.value
  )

  /**
    * @param path that could be relative to APP_HOME
    * @return path relative to APP_HOME
    */
  protected def cleanApplicationIniPath(path: String): String =
    path.stripPrefix("%APP_HOME%\\").stripPrefix("/").replace('\\', '/')

  /**
    * Bat script replacements
    */
  object Replacements {

    def apply(name: String): Seq[(String, String)] =
      Seq("APP_NAME" -> name, "APP_ENV_NAME" -> NameHelper.makeEnvFriendlyName(name))

    def appDefines(mainClass: String,
                   config: BatScriptConfig,
                   replacements: Seq[(String, String)]): (String, String) = {
      val defines = Seq(makeWindowsRelativeClasspathDefine(config.scriptClasspath), Defines.mainClass(mainClass)) ++
        config.configLocation.map(Defines.configFileDefine) ++
        config.extraDefines
      "APP_DEFINES" -> Defines(defines, replacements)
    }

    private[this] def makeWindowsRelativeClasspathDefine(cp: Seq[String]): String = {
      def cleanPath(path: String): String = path.replaceAll("/", "\\\\")
      def isAbsolute(path: String): Boolean =
        path.length > 3 && // check path len is long enough to hold a windows absolute path ("c:\ ...")
          Character.isLetter(path(0)) &&
          path(1) == ':'

      def makeRelativePath(path: String): String = "%APP_LIB_DIR%\\" + cleanPath(path)

      "set \"APP_CLASSPATH=" + (cp map { path =>
        if (isAbsolute(path)) path else makeRelativePath(path)
      } mkString ";") + "\""
    }
  }

  /**
    * Bat defines
    */
  object Defines {
    def apply(defines: Seq[String], replacements: Seq[(String, String)]): String =
      defines.map(replace(_, replacements)).mkString("\r\n")

    def mainClass(mainClass: String): String = s"""set "APP_MAIN_CLASS=$mainClass""""
    def configFileDefine(configFile: String): String = s"""set "SCRIPT_CONF_FILE=$configFile""""

    // TODO - use more of the template writer for this...
    private[this] def replace(line: String, replacements: Seq[(String, String)]): String =
      replacements.foldLeft(line) {
        case (in, (key, value)) => in.replaceAll("@@" + key + "@@", java.util.regex.Matcher.quoteReplacement(value))
      }
  }

  override protected[this] def createReplacementsForMainScript(mainClass: String,
                                                               mainClasses: Seq[String],
                                                               config: SpecializedScriptConfig): Seq[(String, String)] =
    config.replacements :+ Replacements.appDefines(mainClass, config, config.replacements)

}
