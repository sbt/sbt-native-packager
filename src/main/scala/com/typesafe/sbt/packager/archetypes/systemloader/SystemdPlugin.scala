package com.typesafe.sbt.packager.archetypes.systemloader

import sbt._
import sbt.Keys.{sourceDirectory, target}
import com.typesafe.sbt.packager.Keys.{
  defaultLinuxStartScriptLocation,
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
  serverLoading,
  startRunlevels,
  stopRunlevels
}
import com.typesafe.sbt.SbtNativePackager.{Debian, Linux, Rpm, Universal}
import com.typesafe.sbt.packager.archetypes.MaintainerScriptHelper.maintainerScriptsAppend
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.debian.DebianPlugin.autoImport.DebianConstants
import com.typesafe.sbt.packager.rpm.RpmPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin.autoImport.RpmConstants

import java.nio.file.{Files, Paths}

object SystemdPlugin extends AutoPlugin {

  override def requires = SystemloaderPlugin

  object autoImport {
    val systemdSuccessExitStatus =
      settingKey[Seq[String]]("SuccessExitStatus property")
    val systemdIsServiceFileConfig =
      settingKey[Boolean]("Make app_name.service file as config.")
  }

  import autoImport._

  override def projectSettings: Seq[Setting[_]] =
    debianSettings ++ inConfig(Debian)(systemdSettings) ++ rpmSettings ++ inConfig(Rpm)(systemdSettings)

  def systemdSettings: Seq[Setting[_]] = Seq(
    // used by other archetypes to define systemloader dependent behaviour
    serverLoading := Some(ServerLoader.Systemd),
    // Systemd settings
    startRunlevels := None,
    stopRunlevels := None,
    requiredStartFacilities := Some("network.target"),
    requiredStopFacilities := Some("network.target"),
    systemdSuccessExitStatus := Seq.empty,
    linuxStartScriptName := Some(packageName.value + ".service"),
    systemdIsServiceFileConfig := true,
    // add systemloader to mappings
    linuxPackageMappings ++= startScriptMapping(
      linuxStartScriptName.value,
      linuxMakeStartScript.value,
      defaultLinuxStartScriptLocation.value,
      systemdIsServiceFileConfig.value
    ),
    // add additional system configurations to script replacements
    linuxScriptReplacements += ("SuccessExitStatus" -> systemdSuccessExitStatus.value.mkString(" ")),
    linuxScriptReplacements += ("TimeoutStopSec" -> killTimeout.value.toString)
  )

  def debianSettings: Seq[Setting[_]] = inConfig(Debian)(defaultLinuxStartScriptLocation := "/lib/systemd/system")

  def rpmSettings: Seq[Setting[_]] = inConfig(Rpm)(defaultLinuxStartScriptLocation := "/usr/lib/systemd/system")

}
