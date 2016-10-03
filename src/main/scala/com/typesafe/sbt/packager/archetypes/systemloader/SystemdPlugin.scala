package com.typesafe.sbt.packager.archetypes.systemloader

import sbt._
import sbt.Keys.{target, sourceDirectory}
import com.typesafe.sbt.packager.Keys.{
  maintainerScripts,
  packageName,
  linuxStartScriptName,
  linuxStartScriptTemplate,
  linuxMakeStartScript,
  linuxScriptReplacements,
  linuxPackageMappings,
  defaultLinuxStartScriptLocation,
  serverLoading,
  startRunlevels,
  stopRunlevels,
  requiredStartFacilities,
  requiredStopFacilities
}
import com.typesafe.sbt.SbtNativePackager.{Debian, Rpm, Universal, Linux}
import com.typesafe.sbt.packager.archetypes.MaintainerScriptHelper.maintainerScriptsAppend
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.debian.DebianPlugin.autoImport.DebianConstants
import com.typesafe.sbt.packager.rpm.RpmPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin.autoImport.RpmConstants

import java.nio.file.{Paths, Files}

object SystemdPlugin extends AutoPlugin {

  override def requires = SystemloaderPlugin

  object autoImport {
    val systemdSuccessExitStatus =
      settingKey[Seq[String]]("SuccessExitStatus property")
  }

  import autoImport._

  override def projectSettings: Seq[Setting[_]] =
    debianSettings ++ inConfig(Debian)(systemdSettings) ++ rpmSettings ++ inConfig(
      Rpm)(systemdSettings)

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
    // add systemloader to mappings
    linuxPackageMappings ++= startScriptMapping(
      linuxStartScriptName.value,
      linuxMakeStartScript.value,
      defaultLinuxStartScriptLocation.value,
      isConf = true
    ),
    // add additional system configurations to script replacements
    linuxScriptReplacements += ("SuccessExitStatus" -> systemdSuccessExitStatus.value
      .mkString(" "))
  )

  def debianSettings: Seq[Setting[_]] = inConfig(Debian)(
    defaultLinuxStartScriptLocation := "/lib/systemd/system"
  )

  def rpmSettings: Seq[Setting[_]] = inConfig(Rpm)(
    defaultLinuxStartScriptLocation := "/usr/lib/systemd/system"
  )

}
