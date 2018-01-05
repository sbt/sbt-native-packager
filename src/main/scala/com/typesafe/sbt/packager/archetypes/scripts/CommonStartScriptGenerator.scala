package com.typesafe.sbt.packager.archetypes.scripts

import java.io.File
import java.net.URL

import com.typesafe.sbt.packager.archetypes.TemplateWriter
import sbt._

trait CommonStartScriptGenerator {

  /**
    * Script destination in final package
    */
  protected[this] val scriptTargetFolder: String = "bin"

  /**
    * Suffix to append to the generated script name (such as ".bat")
    */
  protected[this] val scriptSuffix: String

  /**
    * Name of the forwarder template if user wants to provide custom one
    */
  protected[this] val forwarderTemplateName: String

  /**
    * Line separator for generated scripts
    */
  protected[this] val eol: String

  /**
    * keySurround for TemplateWriter.generateScript()
    */
  protected[this] val keySurround: String => String

  protected[this] val makeScriptsExecutable: Boolean

  protected[this] def createReplacementsForMainScript(mainClass: String,
                                                      mainClasses: Seq[String],
                                                      config: SpecializedScriptConfig): Seq[(String, String)]

  protected[this] trait ScriptConfig {
    val executableScriptName: String
    val scriptClasspath: Seq[String]
    val replacements: Seq[(String, String)]
    val templateLocation: File

    def withScriptName(scriptName: String): SpecializedScriptConfig
  }

  protected[this] type SpecializedScriptConfig <: ScriptConfig

  protected[this] def generateStartScripts(config: SpecializedScriptConfig,
                                           mainClass: Option[String],
                                           discoveredMainClasses: Seq[String],
                                           targetDir: File,
                                           log: sbt.Logger): Seq[(File, String)] =
    StartScriptMainClassConfig.from(mainClass, discoveredMainClasses) match {
      case NoMain =>
        log.warn("You have no main class in your project. No start script will be generated.")
        Seq.empty
      case SingleMain(main) =>
        Seq(createMainScript(main, config, targetDir, Seq(main)))
      case MultipleMains(mains) =>
        generateMainScripts(mains, config, targetDir)
      case ExplicitMainWithAdditional(main, additional) =>
        createMainScript(main, config, targetDir, discoveredMainClasses) +:
          createForwarderScripts(config.executableScriptName, additional, targetDir)
    }

  private[this] def generateMainScripts(discoveredMainClasses: Seq[String],
                                        config: SpecializedScriptConfig,
                                        targetDir: File): Seq[(File, String)] =
    ScriptUtils.createScriptNames(discoveredMainClasses).map {
      case (qualifiedClassName, scriptName) =>
        val newConfig = config.withScriptName(scriptName)
        createMainScript(qualifiedClassName, newConfig, targetDir, discoveredMainClasses)
    }

  /**
    *
    * @param mainClass - Main class added to the java command
    * @param config - Config data for this script
    * @param targetDir - Target directory for this script
    * @return File pointing to the created main script
    */
  private[this] def createMainScript(mainClass: String,
                                     config: SpecializedScriptConfig,
                                     targetDir: File,
                                     mainClasses: Seq[String]): (File, String) = {
    val template = resolveTemplate(config.templateLocation)
    val replacements = createReplacementsForMainScript(mainClass, mainClasses, config)
    val scriptContent = TemplateWriter.generateScript(template, replacements, eol, keySurround)
    val scriptNameWithSuffix = config.executableScriptName + scriptSuffix
    val script = targetDir / scriptTargetFolder / scriptNameWithSuffix
    IO.write(script, scriptContent)
    // TODO - Better control over this!
    if (makeScriptsExecutable)
      script.setExecutable(true)
    script -> s"$scriptTargetFolder/$scriptNameWithSuffix"
  }

  private[this] def resolveTemplate(defaultTemplateLocation: File): URL =
    if (defaultTemplateLocation.exists) defaultTemplateLocation.toURI.toURL
    else getClass.getResource(defaultTemplateLocation.getName)

  private[this] def createForwarderScripts(executableScriptName: String,
                                           discoveredMainClasses: Seq[String],
                                           targetDir: File): Seq[(File, String)] = {
    val tmp = targetDir / scriptTargetFolder
    val forwarderTemplate = getClass.getResource(forwarderTemplateName)
    ScriptUtils.createScriptNames(discoveredMainClasses).map {
      case (qualifiedClassName, scriptNameWithoutSuffix) =>
        val scriptName = scriptNameWithoutSuffix + scriptSuffix
        val file = tmp / scriptName

        val replacements = Seq("startScript" -> executableScriptName, "qualifiedClassName" -> qualifiedClassName)
        val scriptContent = TemplateWriter.generateScript(forwarderTemplate, replacements, eol, keySurround)

        IO.write(file, scriptContent)
        if (makeScriptsExecutable)
          file.setExecutable(true)
        file -> s"$scriptTargetFolder/$scriptName"
    }
  }
}
