package com.typesafe.sbt.packager.archetypes.systemloader

import sbt._
import sbt.Keys.{sourceDirectory, target}
import com.typesafe.sbt.SbtNativePackager.{Debian, Rpm}
import com.typesafe.sbt.packager.Keys.{
  defaultLinuxStartScriptLocation,
  fileDescriptorLimit,
  killTimeout,
  linuxMakeStartScript,
  linuxPackageMappings,
  linuxScriptReplacements,
  linuxStartScriptName,
  linuxStartScriptTemplate,
  maintainerScripts,
  packageName,
  requiredStartFacilities,
  requiredStopFacilities,
  retries,
  retryTimeout,
  serverLoading,
  serviceAutostart,
  startRunlevels,
  stopRunlevels,
  termTimeout
}
import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.archetypes.MaintainerScriptHelper.maintainerScriptsAppend
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.debian.DebianPlugin.autoImport.DebianConstants
import com.typesafe.sbt.packager.rpm.RpmPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin.autoImport.RpmConstants
import ServerLoader.{ServerLoader, Upstart}

/**
  * General settings for all systemloader plugins.
  */
object SystemloaderPlugin extends AutoPlugin {

  override def requires = DebianPlugin && RpmPlugin

  object autoImport extends SystemloaderKeys {
    val ServerLoader =
      com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader
  }

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Debian)(systemloaderSettings) ++ debianSettings ++
      inConfig(Rpm)(systemloaderSettings) ++ rpmSettings

  def systemloaderSettings: Seq[Setting[_]] = Seq(
    serverLoading := None,
    serviceAutostart := true,
    linuxStartScriptName := Some(packageName.value),
    // defaults, may be override by concrete systemloader
    retries := 0,
    retryTimeout := 60,
    killTimeout := 5,
    termTimeout := 5,
    // add loader-functions to script replacements
    linuxScriptReplacements += loaderFunctionsReplacement(sourceDirectory.value, serverLoading.value),
    linuxScriptReplacements ++= makeStartScriptReplacements(
      requiredStartFacilities = requiredStartFacilities.value,
      requiredStopFacilities = requiredStopFacilities.value,
      startRunlevels = startRunlevels.value,
      stopRunlevels = stopRunlevels.value,
      termTimeout = termTimeout.value,
      killTimeout = killTimeout.value,
      retries = retries.value,
      retryTimeout = retryTimeout.value,
      loader = serverLoading.value
    ),
    // set the template
    linuxStartScriptTemplate := linuxStartScriptUrl(sourceDirectory.value, serverLoading.value),
    // define task to generate the systemloader script
    linuxMakeStartScript := makeStartScript(
      linuxStartScriptTemplate.value,
      linuxScriptReplacements.value,
      (target in Universal).value,
      defaultLinuxStartScriptLocation.value,
      linuxStartScriptName.value.getOrElse(sys.error("`linuxStartScriptName` is not defined"))
    )
  )

  def addAndStartService(autostart: Boolean, pad: String = ""): String = {
    val addService =
      s"""${pad}addService $${{app_name}} || echo "$${{app_name}} could not be registered""""
    val startService =
      s"""${pad}startService $${{app_name}} || echo "$${{app_name}} could not be started""""
    if (autostart) s"${addService}\n${startService}" else addService
  }

  def debianSettings: Seq[Setting[_]] =
    inConfig(Debian)(
      Seq(
        // add automatic service start/stop
        maintainerScripts := maintainerScriptsAppend(maintainerScripts.value, linuxScriptReplacements.value)(
          DebianConstants.Postinst -> s"""|# ${getOrUnsupported(serverLoading.value)} support
                                        |$${{loader-functions}}
                                        |${addAndStartService(serviceAutostart.value)}
                                        |""".stripMargin,
          DebianConstants.Prerm -> s"""|# ${getOrUnsupported(serverLoading.value)} support
                                     |$${{loader-functions}}
                                     |stopService $${{app_name}} || echo "$${{app_name}} wasn't even running!"
                                     |""".stripMargin
        )
      )
    )

  def rpmSettings: Seq[Setting[_]] =
    inConfig(Rpm)(
      Seq(
        // add automatic service start/stop
        maintainerScripts in Rpm := maintainerScriptsAppend(maintainerScripts.value, linuxScriptReplacements.value)(
          RpmConstants.Post -> s"""|# ${getOrUnsupported(serverLoading.value)} support
                                 |$${{loader-functions}}
                                 |# Scriptlet syntax: http://fedoraproject.org/wiki/Packaging:ScriptletSnippets#Syntax
                                 |# $$1 == 1 is first installation and $$1 == 2 is upgrade
                                 |if [ $$1 -eq 1 ] ;
                                 |then
                                 |${addAndStartService(serviceAutostart.value, "  ")}
                                 |fi
                                 |""".stripMargin,
          RpmConstants.Postun -> s"""|# ${getOrUnsupported(serverLoading.value)} support
                                   |if [ $$1 -ge 1 ] ;
                                   |then
                                   |  restartService $${{app_name}} || echo "Failed to try-restart $${{app_name}}"
                                   |fi
                                   |""".stripMargin,
          RpmConstants.Preun -> s"""|# ${getOrUnsupported(serverLoading.value)} support
                                  |$${{loader-functions}}
                                  |if [ $$1 -eq 0 ] ;
                                  |then
                                  |  stopService $${{app_name}} || echo "Could not stop $${{app_name}}"
                                  |fi
                                  |""".stripMargin
        )
      )
    )

  private[this] def makeStartScriptReplacements(requiredStartFacilities: Option[String],
                                                requiredStopFacilities: Option[String],
                                                startRunlevels: Option[String],
                                                stopRunlevels: Option[String],
                                                termTimeout: Int,
                                                killTimeout: Int,
                                                retries: Int,
                                                retryTimeout: Int,
                                                loader: Option[ServerLoader]): Seq[(String, String)] = {

    // Upstart cannot handle empty values
    val (startOn, stopOn) = loader match {
      case Some(Upstart) =>
        (requiredStartFacilities.map("start on started " + _), requiredStopFacilities.map("stop on stopping " + _))
      case _ => (requiredStartFacilities, requiredStopFacilities)
    }
    Seq(
      "start_runlevels" -> startRunlevels.getOrElse(""),
      "stop_runlevels" -> stopRunlevels.getOrElse(""),
      "start_facilities" -> startOn.getOrElse(""),
      "stop_facilities" -> stopOn.getOrElse(""),
      "term_timeout" -> termTimeout.toString,
      "kill_timeout" -> killTimeout.toString,
      "retries" -> retries.toString,
      "retryTimeout" -> retryTimeout.toString
    )
  }

  private def getOrUnsupported(serverLoader: Option[ServerLoader]): String =
    serverLoader.map(_.toString).getOrElse("No system loader")

}
