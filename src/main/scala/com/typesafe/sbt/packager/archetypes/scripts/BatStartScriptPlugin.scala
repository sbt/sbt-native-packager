package com.typesafe.sbt.packager.archetypes.scripts

import java.io.File

import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.archetypes.{JavaAppPackaging, TemplateWriter}
import com.typesafe.sbt.packager.windows.NameHelper
import sbt.Keys._
import sbt._

/**
  * ==Bat StartScript Plugin==
  *
  * This plugins creates a start bat script to run an application built with the
  * [[com.typesafe.sbt.packager.archetypes.JavaAppPackaging]].
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

  protected[this] case class BatScriptConfig(
    override val executableScriptName: String,
    override val scriptClasspath: Seq[String],
    configLocation: Option[String],
    extraDefines: Seq[String],
    override val replacements: Seq[(String, String)],
    override val templateLocation: File,
    bundledJvmLocation: Option[String],
    override val forwarderTemplateLocation: Option[File]
  ) extends ScriptConfig {

    @deprecated("1.3.21", "")
    def this(
      executableScriptName: String,
      scriptClasspath: Seq[String],
      configLocation: Option[String],
      extraDefines: Seq[String],
      replacements: Seq[(String, String)],
      templateLocation: File
    ) =
      this(
        executableScriptName,
        scriptClasspath,
        configLocation,
        extraDefines,
        replacements,
        templateLocation,
        None,
        None
      )

    @deprecated("1.3.21", "")
    def copy(
      executableScriptName: String = executableScriptName,
      scriptClasspath: Seq[String] = scriptClasspath,
      configLocation: Option[String] = configLocation,
      extraDefines: Seq[String] = extraDefines,
      replacements: Seq[(String, String)] = replacements,
      templateLocation: File = templateLocation
    ): BatScriptConfig =
      BatScriptConfig(
        executableScriptName,
        scriptClasspath,
        configLocation,
        extraDefines,
        replacements,
        templateLocation,
        bundledJvmLocation,
        forwarderTemplateLocation
      )

    override def withScriptName(scriptName: String): BatScriptConfig = copy(executableScriptName = scriptName)
  }

  object BatScriptConfig
      extends scala.runtime.AbstractFunction6[String, Seq[String], Option[String], Seq[String], Seq[
        (String, String)
      ], File, BatScriptConfig] {

    @deprecated("1.3.21", "")
    def apply(
      executableScriptName: String,
      scriptClasspath: Seq[String],
      configLocation: Option[String],
      extraDefines: Seq[String],
      replacements: Seq[(String, String)],
      templateLocation: File
    ): BatScriptConfig =
      BatScriptConfig(
        executableScriptName,
        scriptClasspath,
        configLocation,
        extraDefines,
        replacements,
        templateLocation,
        None,
        None
      )

  }

  override protected[this] type SpecializedScriptConfig = BatScriptConfig

  override def projectSettings: Seq[Setting[_]] =
    Seq(
      batScriptTemplateLocation := (sourceDirectory.value / "templates" / batTemplate),
      batForwarderTemplateLocation := Some(sourceDirectory.value / "templates" / forwarderTemplateName),
      batScriptConfigLocation := (batScriptConfigLocation ?? Some(appIniLocation)).value,
      batScriptExtraDefines := Nil,
      batScriptReplacements := Replacements(executableScriptName.value),
      // Generating the application configuration
      Universal / mappings := generateApplicationIni(
        (Universal / mappings).value,
        (Universal / javaOptions).value,
        batScriptConfigLocation.value,
        (Universal / target).value,
        streams.value.log
      ),
      makeBatScripts := generateStartScripts(
        BatScriptConfig(
          executableScriptName = executableScriptName.value,
          scriptClasspath = (batScriptReplacements / scriptClasspath).value,
          configLocation = batScriptConfigLocation.value,
          extraDefines = batScriptExtraDefines.value,
          replacements = batScriptReplacements.value,
          templateLocation = batScriptTemplateLocation.value,
          bundledJvmLocation = bundledJvmLocation.value,
          forwarderTemplateLocation = batForwarderTemplateLocation.value
        ),
        (Compile / batScriptReplacements / mainClass).value,
        (Compile / discoveredMainClasses).value,
        (Universal / target).value / "scripts",
        streams.value.log
      ),
      Universal / mappings ++= makeBatScripts.value
    )

  /**
    * @param path
    *   that could be relative to APP_HOME
    * @return
    *   path relative to APP_HOME
    */
  protected def cleanApplicationIniPath(path: String): String =
    path.stripPrefix("%APP_HOME%\\").stripPrefix("/").replace('\\', '/')

  /**
    * Bat script replacements
    */
  object Replacements {

    def apply(name: String): Seq[(String, String)] =
      Seq("APP_NAME" -> name, "APP_ENV_NAME" -> NameHelper.makeEnvFriendlyName(name))

    def appDefines(
      mainClass: String,
      config: BatScriptConfig,
      replacements: Seq[(String, String)]
    ): (String, String) = {
      val defines = Seq(makeWindowsRelativeClasspathDefine(config.scriptClasspath), Defines.mainClass(mainClass)) ++
        config.configLocation.map(Defines.configFileDefine) ++
        config.bundledJvmLocation.map(Defines.bundledJvmDefine) ++
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
    def bundledJvmDefine(bundledJvm: String): String =
      s"""set "BUNDLED_JVM=%APP_HOME%\\$bundledJvm"""

    // TODO - use more of the template writer for this...
    private[this] def replace(line: String, replacements: Seq[(String, String)]): String =
      replacements.foldLeft(line) { case (in, (key, value)) =>
        in.replaceAll("@@" + key + "@@", java.util.regex.Matcher.quoteReplacement(value))
      }
  }

  override protected[this] def createReplacementsForMainScript(
    mainClass: String,
    mainClasses: Seq[String],
    config: SpecializedScriptConfig
  ): Seq[(String, String)] =
    config.replacements :+ Replacements.appDefines(mainClass, config, config.replacements)

}
