package com.typesafe.sbt.packager.archetypes.systemloader

import sbt.{*, given}
import com.typesafe.sbt.packager.Keys.{
  defaultLinuxStartScriptLocation,
  killTimeout,
  linuxMakeStartScript,
  linuxPackageMappings,
  linuxScriptReplacements,
  linuxStartScriptName,
  packageName,
  requiredStartFacilities,
  requiredStopFacilities,
  serverLoading,
  startRunlevels,
  stopRunlevels
}
import com.typesafe.sbt.SbtNativePackager.{Debian, Rpm}

object SystemdPlugin extends AutoPlugin {

  override def requires = SystemloaderPlugin

  object autoImport {
    val systemdSuccessExitStatus =
      settingKey[Seq[String]]("SuccessExitStatus property")
    val systemdIsServiceFileConfig =
      settingKey[Boolean]("Make app_name.service file as config.")
  }

  import autoImport._

  override def projectSettings: Seq[Setting[?]] =
    debianSettings ++ inConfig(Debian)(systemdSettings) ++ rpmSettings ++ inConfig(Rpm)(systemdSettings)

  def systemdSettings: Seq[Setting[?]] =
    Seq(
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

  def debianSettings: Seq[Setting[?]] = inConfig(Debian)(defaultLinuxStartScriptLocation := "/lib/systemd/system")

  def rpmSettings: Seq[Setting[?]] = inConfig(Rpm)(defaultLinuxStartScriptLocation := "/usr/lib/systemd/system")

}
