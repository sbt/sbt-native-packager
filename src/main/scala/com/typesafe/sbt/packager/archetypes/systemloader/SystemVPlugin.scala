package com.typesafe.sbt.packager.archetypes.systemloader

import sbt._
import sbt.Keys.{target, sourceDirectory}
import com.typesafe.sbt.SbtNativePackager.{Debian, Rpm}
import com.typesafe.sbt.packager.Keys.{
  serverLoading,
  linuxStartScriptTemplate,
  linuxMakeStartScript,
  linuxPackageMappings,
  linuxStartScriptName,
  defaultLinuxStartScriptLocation,
  requiredStartFacilities,
  requiredStopFacilities,
  startRunlevels,
  stopRunlevels,
  killTimeout,
  termTimeout
}
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin

object SystemVPlugin extends AutoPlugin {

  override def requires = SystemloaderPlugin

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Debian)(systemVSettings) ++ debianSettings ++
      inConfig(Rpm)(systemVSettings) ++ rpmSettings

  def systemVSettings: Seq[Setting[_]] = Seq(
    // used by other archetypes to define systemloader dependent behaviour
    serverLoading := Some(ServerLoader.SystemV),
    // Systemd settings
    startRunlevels := Some("2 3 4 5"),
    stopRunlevels := Some("0 1 6"),
    requiredStartFacilities := Some("$remote_fs $syslog"),
    requiredStopFacilities := Some("$remote_fs $syslog"),
    defaultLinuxStartScriptLocation := "/etc/init.d",
    termTimeout := 60,
    killTimeout := 30,
    // add systemloader to mappings and override the isConf setting
    linuxPackageMappings ++= startScriptMapping(
      linuxStartScriptName.value,
      linuxMakeStartScript.value,
      defaultLinuxStartScriptLocation.value,
      isConf = false
    )
  )

  def debianSettings: Seq[Setting[_]] =
    inConfig(Debian)(
      Seq(
        // set the template
        linuxStartScriptTemplate := linuxStartScriptUrl(
          (sourceDirectory in Compile).value,
          serverLoading.value,
          "start-debian-template"
        )
      ))

  def rpmSettings: Seq[Setting[_]] =
    inConfig(Rpm)(
      Seq(
        // set the template
        linuxStartScriptTemplate := linuxStartScriptUrl(
          (sourceDirectory in Compile).value,
          serverLoading.value,
          "start-rpm-template"
        )
      ))

}
