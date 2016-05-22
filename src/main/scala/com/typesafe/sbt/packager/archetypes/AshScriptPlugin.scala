package com.typesafe.sbt
package packager
package archetypes

import sbt._
import sbt.Keys.{ mappings, target, name, mainClass, sourceDirectory, javaOptions, streams }
import packager.Keys.{ packageName, executableScriptName }
import linux.{ LinuxFileMetaData, LinuxPackageMapping }
import linux.LinuxPlugin.autoImport.{ linuxPackageMappings, defaultLinuxInstallLocation }
import SbtNativePackager.{ Universal, Debian }

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

  override def requires = JavaAppPackaging

  import JavaAppPackaging.autoImport._

  val ashTemplate = "ash-template"

  override def projectSettings = Seq(
    bashScriptTemplateLocation := (sourceDirectory.value / "templates" / ashTemplate),
    makeBashScript <<= (bashScriptTemplateLocation, bashScriptDefines, target in Universal, executableScriptName, sourceDirectory) map makeUniversalAshScript,
    bashScriptDefines <<= (Keys.mainClass in (Compile, bashScriptDefines), scriptClasspath in bashScriptDefines, bashScriptExtraDefines, bashScriptConfigLocation) map { (mainClass, cp, extras, config) =>
      val hasMain =
        for {
          cn <- mainClass
        } yield JavaAppAshScript.makeDefines(cn, appClasspath = cp, extras = extras, configFile = config)
      hasMain getOrElse Nil
    }
  )

  def makeUniversalAshScript(defaultTemplateLocation: File, defines: Seq[String], tmpDir: File, name: String, sourceDir: File): Option[File] =
    if (defines.isEmpty) None
    else {
      val template = resolveTemplate(defaultTemplateLocation)
      val scriptBits = JavaAppAshScript.generateScript(defines, template)
      val script = tmpDir / "tmp" / "bin" / name
      IO.write(script, scriptBits)
      // TODO - Better control over this!
      script.setExecutable(true)
      Some(script)
    }

  private def resolveTemplate(defaultTemplateLocation: File): URL = {
    if (defaultTemplateLocation.exists)
      defaultTemplateLocation.toURI.toURL
    else
      getClass.getResource(defaultTemplateLocation.getName)
  }
}
