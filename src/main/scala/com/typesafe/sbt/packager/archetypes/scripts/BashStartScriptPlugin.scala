package com.typesafe.sbt.packager.archetypes.scripts

import java.io.File
import java.net.URL

import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.archetypes.{JavaAppPackaging, TemplateWriter}
import sbt.Keys._
import sbt._

/**
  * == Bash StartScript Plugin ==
  *
  * This plugins creates a start bash script to run an application built with the
  * [[com.typesafe.sbt.packager.archetypes.JavaAppPackaging]].
  *
  */
object BashStartScriptPlugin extends AutoPlugin {

  /**
    * Name of the bash template if user wants to provide custom one
    */
  val bashTemplate = "bash-template"

  /**
    * Name of the bash forwarder template if user wants to provide custom one
    */
  val bashForwarderTemplate = "bash-forwarder-template"

  /**
    * Location for the application.ini file used by the bash script to load initialization parameters for jvm and app
    */
  val appIniLocation = "${app_home}/../conf/application.ini"

  /**
    * Script destination in final package
    */
  val scriptTargetFolder = "bin"

  override val requires = JavaAppPackaging
  override val trigger = AllRequirements

  object autoImport extends BashStartScriptKeys

  private[this] case class BashScriptConfig(executableScriptName: String,
                                            scriptClasspath: Seq[String],
                                            bashScriptReplacements: Seq[(String, String)],
                                            bashScriptTemplateLocation: File)

  override def projectSettings: Seq[Setting[_]] = Seq(
    bashScriptTemplateLocation := (sourceDirectory.value / "templates" / bashTemplate),
    bashScriptExtraDefines := Nil,
    bashScriptDefines := Defines((scriptClasspath in bashScriptDefines).value, bashScriptConfigLocation.value),
    bashScriptDefines ++= bashScriptExtraDefines.value,
    bashScriptReplacements := generateScriptReplacements(bashScriptDefines.value),
    // Create a bashConfigLocation if options are set in build.sbt
    bashScriptConfigLocation := (bashScriptConfigLocation ?? Some(appIniLocation)).value,
    bashScriptEnvConfigLocation := (bashScriptEnvConfigLocation ?? None).value,
    // Generating the application configuration
    mappings in Universal := generateApplicationIni(
      (mappings in Universal).value,
      (javaOptions in Universal).value,
      bashScriptConfigLocation.value,
      (target in Universal).value,
      streams.value.log
    ),
    makeBashScripts := generateStartScripts(
      BashScriptConfig(
        executableScriptName = executableScriptName.value,
        scriptClasspath = (scriptClasspath in bashScriptDefines).value,
        bashScriptReplacements = bashScriptReplacements.value,
        bashScriptTemplateLocation = bashScriptTemplateLocation.value
      ),
      (mainClass in (Compile, bashScriptDefines)).value,
      (discoveredMainClasses in Compile).value,
      (target in Universal).value / "scripts",
      streams.value.log
    ),
    mappings in Universal ++= makeBashScripts.value
  )

  private[this] def generateScriptReplacements(defines: Seq[String]): Seq[(String, String)] = {
    val defineString = defines mkString "\n"
    Seq("template_declares" -> defineString)
  }

  private[this] def generateApplicationIni(universalMappings: Seq[(File, String)],
                                           javaOptions: Seq[String],
                                           bashScriptConfigLocation: Option[String],
                                           tmpDir: File,
                                           log: Logger): Seq[(File, String)] =
    bashScriptConfigLocation
      .collect {
        case location if javaOptions.nonEmpty =>
          val configFile = tmpDir / "tmp" / "conf" / "application.ini"
          //Do not use writeLines here because of issue #637
          IO.write(configFile, ("# options from build" +: javaOptions).mkString("\n"))
          val filteredMappings = universalMappings.filter {
            case (file, path) => path != appIniLocation
          }
          // Warn the user if he tries to specify options
          if (filteredMappings.size < universalMappings.size) {
            log.warn("--------!!! JVM Options are defined twice !!!-----------")
            log.warn(
              "application.ini is already present in output package. Will be overridden by 'javaOptions in Universal'"
            )
          }
          (configFile -> cleanApplicationIniPath(location)) +: filteredMappings

      }
      .getOrElse(universalMappings)

  private[this] def generateStartScripts(config: BashScriptConfig,
                                         mainClass: Option[String],
                                         discoveredMainClasses: Seq[String],
                                         targetDir: File,
                                         log: Logger): Seq[(File, String)] =
    StartScriptMainClassConfig.from(mainClass, discoveredMainClasses) match {
      case NoMain =>
        log.warn("You have no main class in your project. No start script will be generated.")
        Seq.empty
      case SingleMain(main) =>
        Seq(MainScript(main, config, targetDir, Seq(main)) -> s"$scriptTargetFolder/${config.executableScriptName}")
      case MultipleMains(mains) =>
        generateMainScripts(mains, config, targetDir)
      case ExplicitMainWithAdditional(main, additional) =>
        (MainScript(main, config, targetDir, discoveredMainClasses) -> s"$scriptTargetFolder/${config.executableScriptName}") +:
          ForwarderScripts(config.executableScriptName, additional, targetDir)
    }

  private[this] def generateMainScripts(discoveredMainClasses: Seq[String],
                                        config: BashScriptConfig,
                                        targetDir: File): Seq[(File, String)] =
    discoveredMainClasses.map { qualifiedClassName =>
      val bashConfig =
        config.copy(executableScriptName = makeScriptName(qualifiedClassName))
      MainScript(qualifiedClassName, bashConfig, targetDir, discoveredMainClasses) -> s"$scriptTargetFolder/${bashConfig.executableScriptName}"
    }

  private[this] def makeScriptName(qualifiedClassName: String): String = {
    val clazz = qualifiedClassName.split("\\.").last

    val lowerCased = clazz.drop(1).flatMap {
      case c if c.isUpper => Seq('-', c.toLower)
      case c => Seq(c)
    }

    clazz(0).toLower +: lowerCased
  }

  /**
    * @param path that could be relative to app_home
    * @return path relative to app_home
    */
  private[this] def cleanApplicationIniPath(path: String): String =
    path.replaceFirst("\\$\\{app_home\\}/../", "")

  /**
    * Bash defines
    */
  object Defines {

    /**
      * Creates the block of defines for a script.
      *
      * @param appClasspath A sequence of relative-locations (to the lib/ folder) of jars
      *                     to include on the classpath.
      * @param configFile An (optional) filename from which the script will read arguments.
      */
    def apply(appClasspath: Seq[String], configFile: Option[String]): Seq[String] =
      (configFile map configFileDefine).toSeq ++ Seq(makeClasspathDefine(appClasspath))

    private[this] def makeClasspathDefine(cp: Seq[String]): String = {
      val fullString = cp map (
        n =>
          if (n.startsWith(File.separator)) n
          else "$lib_dir/" + n
      ) mkString ":"
      "declare -r app_classpath=\"" + fullString + "\"\n"
    }

    private[this] def configFileDefine(configFile: String) =
      "declare -r script_conf_file=\"%s\"" format configFile
  }

  object MainScript {

    /**
      *
      * @param mainClass - Main class added to the java command
      * @param config - Config data for this script
      * @param targetDir - Target directory for this script
      * @return File pointing to the created main script
      */
    def apply(mainClass: String, config: BashScriptConfig, targetDir: File, mainClasses: Seq[String]): File = {
      val template = resolveTemplate(config.bashScriptTemplateLocation)
      val replacements = Seq(
        "app_mainclass" -> mainClassReplacement(mainClass),
        "available_main_classes" -> usageMainClassReplacement(mainClasses)
      ) ++ config.bashScriptReplacements

      val scriptContent = TemplateWriter.generateScript(template, replacements)
      val script = targetDir / "scripts" / config.executableScriptName
      IO.write(script, scriptContent)
      // TODO - Better control over this!
      script.setExecutable(true)
      script
    }

    private[this] def mainClassReplacement(mainClass: String): String = {
      val jarPrefixed = """^\-jar (.*)""".r
      val args = mainClass match {
        case jarPrefixed(jarName) => Seq("-jar", jarName)
        case className => Seq(className)
      }
      args.map(s => "\"" + s + "\"").mkString(" ")
    }

    private[this] def usageMainClassReplacement(mainClasses: Seq[String]): String =
      if (mainClasses.nonEmpty)
        mainClasses.mkString("Available main classes:\n\t", "\n\t", "")
      else
        ""

    private[this] def resolveTemplate(defaultTemplateLocation: File): URL =
      if (defaultTemplateLocation.exists) defaultTemplateLocation.toURI.toURL
      else getClass.getResource(defaultTemplateLocation.getName)
  }

  object ForwarderScripts {
    def apply(executableScriptName: String, discoveredMainClasses: Seq[String], targetDir: File): Seq[(File, String)] = {
      val tmp = targetDir / "scripts"
      val forwarderTemplate = getClass.getResource(bashForwarderTemplate)
      discoveredMainClasses.map { qualifiedClassName =>
        val clazz = makeScriptName(qualifiedClassName)
        val file = tmp / clazz

        val replacements = Seq("startScript" -> executableScriptName, "qualifiedClassName" -> qualifiedClassName)
        val scriptContent = TemplateWriter.generateScript(forwarderTemplate, replacements)

        IO.write(file, scriptContent)
        file.setExecutable(true)
        file -> s"bin/$clazz"
      }
    }
  }
}
