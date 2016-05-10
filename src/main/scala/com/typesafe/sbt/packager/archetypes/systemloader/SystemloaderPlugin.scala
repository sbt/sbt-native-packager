package com.typesafe.sbt.packager.archetypes.systemloader

import sbt._
import sbt.Keys.{ sourceDirectory, target }
import com.typesafe.sbt.SbtNativePackager.{ Debian, Rpm }
import com.typesafe.sbt.packager.Keys.{
  packageName,
  maintainerScripts,
  defaultLinuxStartScriptLocation,
  linuxMakeStartScript,
  linuxStartScriptTemplate,
  linuxScriptReplacements,
  linuxStartScriptName,
  linuxPackageMappings,
  serverLoading
}
import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.archetypes.MaintainerScriptHelper.maintainerScriptsAppend
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.debian.DebianPlugin.autoImport.DebianConstants
import com.typesafe.sbt.packager.rpm.RpmPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin.autoImport.RpmConstants
import ServerLoader.{ ServerLoader, Upstart }

/**
 * General settings for all systemloader plugins.
 */
object SystemloaderPlugin extends AutoPlugin {
  
  override def requires = DebianPlugin && RpmPlugin

  object autoImport extends SystemloaderKeys {
    val ServerLoader = com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader
  }

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Debian)(systemloaderSettings) ++ debianSettings
  inConfig(Rpm)(systemloaderSettings) ++ rpmSettings

  def systemloaderSettings: Seq[Setting[_]] = Seq(
    linuxStartScriptName := Some(packageName.value),
    // add loader-functions to script replacements
    linuxScriptReplacements += loaderFunctionsReplacement((sourceDirectory in Compile).value, serverLoading.value),
    // set the template
    linuxStartScriptTemplate := linuxStartScriptUrl((sourceDirectory in Compile).value, serverLoading.value),
    // define task to generate the systemloader script
    linuxMakeStartScript := makeStartScript(
      linuxStartScriptTemplate.value,
      linuxScriptReplacements.value,
      target.value,
      defaultLinuxStartScriptLocation.value,
      linuxStartScriptName.value.getOrElse(sys.error("`linuxStartScriptName` is not defined"))
    ),
    // add systemloader to mappings
    linuxPackageMappings ++= startScriptMapping(
      linuxStartScriptName.value,
      linuxMakeStartScript.value,
      defaultLinuxStartScriptLocation.value
    )
  )

  def debianSettings: Seq[Setting[_]] = inConfig(Debian)(Seq(
    // add automatic service start/stop
    maintainerScripts := maintainerScriptsAppend(
      maintainerScripts.value,
      linuxScriptReplacements.value
    )(
        DebianConstants.Postinst -> s"""|# ${serverLoading.value} support
                                        |$${{loader-functions}}
                                        |startService $${{app_name}} || echo "$${{app_name}} could not be registered or started"
                                        |""".stripMargin,
        DebianConstants.Prerm -> s"""|# ${serverLoading.value} support
                                     |$${{loader-functions}}
                                     |stopService $${{app_name}} || echo "$${{app_name}} wasn't even running!"
                                     |""".stripMargin
      ))
  )

  def rpmSettings: Seq[Setting[_]] = inConfig(Rpm)(Seq(
    // add automatic service start/stop
    maintainerScripts in Rpm := maintainerScriptsAppend(
      maintainerScripts.value,
      linuxScriptReplacements.value
    )(
        RpmConstants.Post -> s"""|# ${serverLoading.value} support
                                 |$${{loader-functions}}
                                 |# Scriptlet syntax: http://fedoraproject.org/wiki/Packaging:ScriptletSnippets#Syntax
                                 |# $$1 == 1 is first installation and $$1 == 2 is upgrade
                                 |if [ $$1 -eq 1 ] ;
                                 |then
                                 |  startService $${{app_name}} || echo "Could not start $${{app_name}}"
                                 |fi
                                 |""".stripMargin,
        RpmConstants.Postun -> s"""|# ${serverLoading.value} support
                                   |if [ $$1 -ge 1 ]
                                   |  restartService $${{app_name}} || echo "Failed to try-restart $${{app_name}}"
                                   |fi
                                   |""".stripMargin,
        RpmConstants.Preun -> s"""|# ${serverLoading.value} support
                                  |$${{loader-functions}}
                                  |if [ $$1 -eq 0 ] ;
                                  |then
                                  |  stopService $${{app_name}} || echo "Could not stop $${{app_name}}"
                                  |fi
                                  |""".stripMargin
      ))
  )

  private[this] def makeStartScriptReplacements(
    requiredStartFacilities: Option[String],
    requiredStopFacilities: Option[String],
    startRunlevels: Option[String],
    stopRunlevels: Option[String],
    loader: ServerLoader): Seq[(String, String)] = {

    // Upstart cannot handle empty values
    val (startOn, stopOn) = loader match {
      case Upstart => (requiredStartFacilities.map("start on started " + _), requiredStopFacilities.map("stop on stopping " + _))
      case _       => (requiredStartFacilities, requiredStopFacilities)
    }
    Seq(
      "start_runlevels" -> startRunlevels.getOrElse(""),
      "stop_runlevels" -> stopRunlevels.getOrElse(""),
      "start_facilities" -> startOn.getOrElse(""),
      "stop_facilities" -> stopOn.getOrElse("")
    )
  }

}