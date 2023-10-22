package com.typesafe.sbt.packager.archetypes.scripts

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
  */
object BashStartScriptPlugin extends AutoPlugin with ApplicationIniGenerator with LinuxStartScriptGenerator {

  /**
    * Name of the bash template if user wants to provide custom one
    */
  val bashTemplate = "bash-template"

  /**
    * Name of the bash forwarder template if user wants to provide custom one
    */
  override protected[this] val forwarderTemplateName = "bash-forwarder-template"

  /**
    * Location for the application.ini file used by the bash script to load initialization parameters for jvm and app
    */
  val appIniLocation = "${app_home}/../conf/application.ini"

  override val requires = JavaAppPackaging
  override val trigger = AllRequirements

  object autoImport extends BashStartScriptKeys

  override def projectSettings: Seq[Setting[_]] =
    Seq(
      bashScriptTemplateLocation := (sourceDirectory.value / "templates" / bashTemplate),
      bashScriptExtraDefines := Nil,
      bashScriptDefines := Defines(
        (scriptClasspath in bashScriptDefines).value,
        bashScriptConfigLocation.value,
        bundledJvmLocation.value
      ),
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
          replacements = bashScriptReplacements.value,
          templateLocation = bashScriptTemplateLocation.value
        ),
        (mainClass in (Compile, bashScriptDefines)).value,
        (discoveredMainClasses in Compile).value,
        (target in Universal).value / "scripts",
        streams.value.log
      ),
      mappings in Universal ++= makeBashScripts.value
    )

  /**
    * @param path that could be relative to app_home
    * @return path relative to app_home
    */
  protected def cleanApplicationIniPath(path: String): String = path.stripPrefix("${app_home}/../")

  /**
    * Bash defines
    */
  object Defines {

    @deprecated("1.3.21", "")
    def apply(appClasspath: Seq[String], configFile: Option[String]): Seq[String] =
      apply(appClasspath, configFile, None)

    /**
      * Creates the block of defines for a script.
      *
      * @param appClasspath A sequence of relative-locations (to the lib/ folder) of jars
      *                     to include on the classpath.
      * @param configFile An (optional) filename from which the script will read arguments.
      */
    def apply(appClasspath: Seq[String], configFile: Option[String], bundledJvm: Option[String]): Seq[String] =
      (configFile map configFileDefine).toSeq ++
        Seq(makeClasspathDefine(appClasspath)) ++
        (bundledJvm map bundledJvmDefine).toSeq

    private[this] def makeClasspathDefine(cp: Seq[String]): String = {
      val fullString = cp map (
        n =>
          if (n.startsWith("/")) n
          else "$lib_dir/" + n
      ) mkString ":"
      "declare -r app_classpath=\"" + fullString + "\"\n"
    }

    private[this] def configFileDefine(configFile: String) =
      "declare -r script_conf_file=\"%s\"" format configFile

    private[this] def bundledJvmDefine(bundledJvm: String) =
      """declare -r bundled_jvm="$(dirname "$app_home")/%s"""" format bundledJvm
  }
}
