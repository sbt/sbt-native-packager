package com.typesafe.sbt.packager.archetypes.scripts

import java.io.File
import java.net.URL

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
object BatStartScriptPlugin extends AutoPlugin with ApplicationIniGenerator {

  /**
    * Name of the bat template if user wants to provide custom one
    */
  val batTemplate = "bat-template"

  /**
    * Name of the bat forwarder template if user wants to provide custom one
    */
  val batForwarderTemplate = "bat-forwarder-template"

  /**
    * Script destination in final package
    */
  val scriptTargetFolder = "bin"

  /**
    * Location for the application.ini file used by the bat script to load initialization parameters for jvm and app
    */
  val appIniLocation = "%APP_HOME%\\conf\\application.ini"

  override val requires = JavaAppPackaging
  override val trigger = AllRequirements

  object autoImport extends BatStartScriptKeys
  import autoImport._

  private[this] case class BatScriptConfig(executableScriptName: String,
                                           scriptClasspath: Seq[String],
                                           configLocation: Option[String],
                                           extraDefines: Seq[String],
                                           replacements: Seq[(String, String)],
                                           batScriptTemplateLocation: File)

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
        executableScriptName = s"${executableScriptName.value}.bat",
        scriptClasspath = (scriptClasspath in batScriptReplacements).value,
        configLocation = batScriptConfigLocation.value,
        extraDefines = batScriptExtraDefines.value,
        replacements = batScriptReplacements.value,
        batScriptTemplateLocation = batScriptTemplateLocation.value
      ),
      (mainClass in (Compile, batScriptReplacements)).value,
      (discoveredMainClasses in Compile).value,
      (target in Universal).value / "scripts",
      streams.value.log
    ),
    mappings in Universal ++= makeBatScripts.value
  )

  private[this] def generateStartScripts(config: BatScriptConfig,
                                         mainClass: Option[String],
                                         discoveredMainClasses: Seq[String],
                                         targetDir: File,
                                         log: Logger): Seq[(File, String)] =
    StartScriptMainClassConfig.from(mainClass, discoveredMainClasses) match {
      case NoMain =>
        log.warn("You have no main class in your project. No start script will be generated.")
        Seq.empty
      case SingleMain(main) =>
        Seq(MainScript(main, config, targetDir) -> s"$scriptTargetFolder/${config.executableScriptName}")
      case MultipleMains(mains) =>
        generateMainScripts(discoveredMainClasses, config, targetDir)
      case ExplicitMainWithAdditional(main, additional) =>
        (MainScript(main, config, targetDir) -> s"$scriptTargetFolder/${config.executableScriptName}") +:
          ForwarderScripts(config.executableScriptName, additional, targetDir)
    }

  private[this] def generateMainScripts(discoveredMainClasses: Seq[String],
                                        config: BatScriptConfig,
                                        targetDir: File): Seq[(File, String)] =
    discoveredMainClasses.map { qualifiedClassName =>
      val batConfig = config.copy(executableScriptName = makeScriptName(qualifiedClassName))
      MainScript(qualifiedClassName, batConfig, targetDir) -> s"$scriptTargetFolder/${batConfig.executableScriptName}"
    }

  private[this] def makeScriptName(qualifiedClassName: String): String = {
    val clazz = qualifiedClassName.split("\\.").last

    val lowerCased = clazz.drop(1).flatMap {
      case c if c.isUpper => Seq('-', c.toLower)
      case c => Seq(c)
    }

    clazz(0).toLower + lowerCased + ".bat"
  }

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

    def appDefines(mainClass: String, config: BatScriptConfig, replacements: Seq[(String, String)]): (String, String) = {
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

  object MainScript {

    /**
      *
      * @param mainClass - Main class added to the java command
      * @param config - Config data for this script
      * @param targetDir - Target directory for this script
      * @return File pointing to the created main script
      */
    def apply(mainClass: String, config: BatScriptConfig, targetDir: File): File = {
      val template = resolveTemplate(config.batScriptTemplateLocation)
      val replacements = config.replacements :+ Replacements.appDefines(mainClass, config, config.replacements)
      val scriptContent =
        TemplateWriter.generateScript(template, replacements, "\r\n", TemplateWriter.batFriendlyKeySurround)
      val script = targetDir / "scripts" / config.executableScriptName
      IO.write(script, scriptContent)
      script
    }

    private[this] def resolveTemplate(defaultTemplateLocation: File): URL =
      if (defaultTemplateLocation.exists) defaultTemplateLocation.toURI.toURL
      else getClass.getResource(defaultTemplateLocation.getName)

  }

  object ForwarderScripts {
    def apply(executableScriptName: String, discoveredMainClasses: Seq[String], targetDir: File): Seq[(File, String)] = {
      val tmp = targetDir / "scripts"
      val forwarderTemplate = getClass.getResource(batForwarderTemplate)
      discoveredMainClasses.map { qualifiedClassName =>
        val scriptName = makeScriptName(qualifiedClassName)
        val file = tmp / scriptName

        val replacements = Seq("startScript" -> executableScriptName, "qualifiedClassName" -> qualifiedClassName)
        val scriptContent =
          TemplateWriter.generateScript(forwarderTemplate, replacements, "\r\n", TemplateWriter.batFriendlyKeySurround)

        IO.write(file, scriptContent)
        file -> s"bin/$scriptName"
      }
    }

  }
}
