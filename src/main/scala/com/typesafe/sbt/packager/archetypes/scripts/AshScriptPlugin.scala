package com.typesafe.sbt.packager.archetypes.scripts

import java.io.File

import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import sbt.Keys.sourceDirectory
import sbt._

/**
  * == Java Application ==
  *
  * This class is an alternate to JavaAppPackaging designed to support the ash shell.  JavaAppPackaging
  * generates bash-specific code that is not compatible with ash, a very stripped-down, lightweight shell
  * used by popular micro base Docker images like BusyBox.  The AshScriptPlugin will generate simple
  * ash-compatible output.
  *
  * Just like with JavaAppPackaging you can override the bash-template file by creating a src/templates
  * directory and adding your own bash-template file.  Actually this isn't a bad idea as the default
  * bash-template file inherited from JavaAppPackaging has a lot of stuff you probably don't want/need
  * in a highly-constrained environment like ash+BusyBox.  Something much simpler will do, for example:
  *
  * #!/usr/bin/env sh
  *
  * realpath () {
  * (
  *   TARGET_FILE="$1"
  *
  *   cd "$(dirname "$TARGET_FILE")"
  *   TARGET_FILE=$(basename "$TARGET_FILE")
  *
  *   COUNT=0
  *   while [ -L "$TARGET_FILE" -a $COUNT -lt 100 ]
  *   do
  *       TARGET_FILE=$(readlink "$TARGET_FILE")
  *       cd "$(dirname "$TARGET_FILE")"
  *       TARGET_FILE=$(basename "$TARGET_FILE")
  *       COUNT=$(($COUNT + 1))
  *   done
  *
  *   if [ "$TARGET_FILE" == "." -o "$TARGET_FILE" == ".." ]; then
  *     cd "$TARGET_FILE"
  *     TARGET_FILEPATH=
  *   else
  *     TARGET_FILEPATH=/$TARGET_FILE
  *   fi
  *
  *   echo "$(pwd -P)/$TARGET_FILE"
  * )
  * }
  *
  * real_script_path="$(realpath "$0")"
  * app_home="$(realpath "$(dirname "$real_script_path")")"
  * lib_dir="$(realpath "${app_home}/../lib")"
  *
  * ${{template_declares}}
  *
  * java -classpath $app_classpath $app_mainclass $@
  *
  *
  * == Configuration ==
  *
  * This plugin adds new settings to configure your packaged application.
  * The keys are defined in [[com.typesafe.sbt.packager.archetypes.JavaAppKeys]]
  *
  * @example Enable this plugin in your `build.sbt` with
  *
  * {{{
  *  enablePlugins(AshScriptPlugin)
  * }}}
  */
object AshScriptPlugin extends AutoPlugin {

  override def requires = JavaAppPackaging && BashStartScriptPlugin

  val ashTemplate = "ash-template"

  override def projectSettings = Seq(
    bashScriptTemplateLocation := (sourceDirectory.value / "templates" / ashTemplate),
    bashScriptDefines := Defines(
      (scriptClasspath in bashScriptDefines).value,
      bashScriptConfigLocation.value,
      bundledJvmLocation.value
    ),
    bashScriptDefines ++= bashScriptExtraDefines.value
  )

  /**
    * Ash defines
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
          if (n.startsWith(File.separator)) n
          else "$lib_dir/" + n
      ) mkString ":"
      "app_classpath=\"" + fullString + "\"\n"
    }

    private[this] def configFileDefine(configFile: String) =
      "script_conf_file=\"%s\"" format (configFile)

    private[this] def bundledJvmDefine(bundledJvm: String) =
      """bundled_jvm="$(realpath "${app_home}/../%s")"""" format bundledJvm
  }
}
