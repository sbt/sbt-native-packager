package com.typesafe.sbt.packager.archetypes.scripts

import java.io.File
import java.net.URL

import com.typesafe.sbt.packager.PluginCompat
import com.typesafe.sbt.packager.archetypes.TemplateWriter
import sbt.{*, given}
import xsbti.FileConverter

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

  /**
    * Set executable bit of the generated scripts to this value
    * @todo
    *   Does it work when building archives on hosts that do not support such permission?
    */
  protected[this] val executableBitValue: Boolean

  protected[this] def createReplacementsForMainScript(
    mainClass: String,
    mainClasses: Seq[String],
    config: SpecializedScriptConfig
  ): Seq[(String, String)]

  protected[this] trait ScriptConfig {
    val executableScriptName: String
    val scriptClasspath: Seq[String]
    val replacements: Seq[(String, String)]
    val templateLocation: File
    val forwarderTemplateLocation: Option[File]

    def withScriptName(scriptName: String): SpecializedScriptConfig
  }

  /**
    * The type of specialized ScriptConfig. This enables callback methods of the concrete plugin implementations to use
    * fields of config that only exist in their ScriptConfig specialization.
    */
  protected[this] type SpecializedScriptConfig <: ScriptConfig

  protected[this] def generateStartScripts(
    config: SpecializedScriptConfig,
    mainClass: Option[String],
    discoveredMainClasses: Seq[String],
    targetDir: File,
    conv: FileConverter,
    log: sbt.Logger
  ): Seq[(PluginCompat.FileRef, String)] =
    StartScriptMainClassConfig.from(mainClass, discoveredMainClasses) match {
      case NoMain =>
        log.warn("You have no main class in your project. No start script will be generated.")
        Seq.empty
      case SingleMain(main) =>
        Seq(createMainScript(main, config, targetDir, Seq(main), conv))
      case MultipleMains(mains) =>
        generateMainScripts(mains, config, targetDir, conv, log)
      case ExplicitMainWithAdditional(main, additional) =>
        createMainScript(main, config, targetDir, discoveredMainClasses, conv) +:
          createForwarderScripts(config.executableScriptName, additional, targetDir, config, conv, log)
    }

  private[this] def generateMainScripts(
    discoveredMainClasses: Seq[String],
    config: SpecializedScriptConfig,
    targetDir: File,
    conv: FileConverter,
    log: sbt.Logger
  ): Seq[(PluginCompat.FileRef, String)] = {
    val classAndScriptNames = ScriptUtils.createScriptNames(discoveredMainClasses)
    ScriptUtils.warnOnScriptNameCollision(classAndScriptNames, log)

    classAndScriptNames
      .find { case (_, script) =>
        script == config.executableScriptName
      }
      .map(_ => classAndScriptNames)
      .getOrElse(
        classAndScriptNames ++ Seq("" -> config.executableScriptName)
      ) // empty string to enforce the custom class in scripts
      .map { case (qualifiedClassName, scriptName) =>
        val newConfig = config.withScriptName(scriptName)
        createMainScript(qualifiedClassName, newConfig, targetDir, discoveredMainClasses, conv)
      }
  }

  private[this] def mainScriptName(config: ScriptConfig): String =
    config.executableScriptName + scriptSuffix

  /**
    * @param mainClass
    *   Main class added to the java command
    * @param config
    *   Config data for this script
    * @param targetDir
    *   Target directory for this script
    * @return
    *   File pointing to the created main script
    */
  private[this] def createMainScript(
    mainClass: String,
    config: SpecializedScriptConfig,
    targetDir: File,
    mainClasses: Seq[String],
    conv0: FileConverter
  ): (PluginCompat.FileRef, String) = {
    implicit val conv: FileConverter = conv0
    val template = resolveTemplate(config.templateLocation)
    val replacements = createReplacementsForMainScript(mainClass, mainClasses, config)
    val scriptContent = TemplateWriter.generateScript(template, replacements, eol, keySurround)
    val scriptNameWithSuffix = mainScriptName(config)
    val script = targetDir / scriptTargetFolder / scriptNameWithSuffix

    IO.write(script, scriptContent)
    // TODO - Better control over this!
    script.setExecutable(executableBitValue)
    val scriptRef = PluginCompat.toFileRef(script)
    scriptRef -> s"$scriptTargetFolder/$scriptNameWithSuffix"
  }

  private[this] def resolveTemplate(templateLocation: File): URL =
    if (templateLocation.exists) templateLocation.toURI.toURL
    else getClass.getResource(templateLocation.getName)

  private[this] def createForwarderScripts(
    executableScriptName: String,
    discoveredMainClasses: Seq[String],
    targetDir: File,
    config: ScriptConfig,
    conv0: FileConverter,
    log: sbt.Logger
  ): Seq[(PluginCompat.FileRef, String)] = {
    implicit val conv: FileConverter = conv0
    val tmp = targetDir / scriptTargetFolder
    val forwarderTemplate =
      config.forwarderTemplateLocation.map(resolveTemplate).getOrElse(getClass.getResource(forwarderTemplateName))
    val classAndScriptNames = ScriptUtils.createScriptNames(discoveredMainClasses)
    ScriptUtils.warnOnScriptNameCollision(classAndScriptNames :+ ("<main script>" -> mainScriptName(config)), log)
    classAndScriptNames.map { case (qualifiedClassName, scriptNameWithoutSuffix) =>
      val scriptName = scriptNameWithoutSuffix + scriptSuffix
      val file = tmp / scriptName

      val replacements = Seq("startScript" -> executableScriptName, "qualifiedClassName" -> qualifiedClassName)
      val scriptContent = TemplateWriter.generateScript(forwarderTemplate, replacements, eol, keySurround)

      IO.write(file, scriptContent)
      file.setExecutable(executableBitValue)
      val fileRef = PluginCompat.toFileRef(file)
      fileRef -> s"$scriptTargetFolder/$scriptName"
    }
  }
}
