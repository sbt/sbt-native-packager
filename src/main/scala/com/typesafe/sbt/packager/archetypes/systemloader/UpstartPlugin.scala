package com.typesafe.sbt.packager.archetypes.systemloader

import sbt._
import sbt.Keys.{sourceDirectory, target}
import com.typesafe.sbt.packager.Keys.{
  defaultLinuxStartScriptLocation,
  killTimeout,
  linuxMakeStartScript,
  linuxPackageMappings,
  linuxStartScriptName,
  linuxStartScriptTemplate,
  packageName,
  requiredStartFacilities,
  requiredStopFacilities,
  serverLoading,
  startRunlevels,
  stopRunlevels
}
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.debian.DebianPlugin.autoImport.Debian
import com.typesafe.sbt.packager.rpm.RpmPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin.autoImport.Rpm

import java.nio.file.{Files, Paths}

object UpstartPlugin extends AutoPlugin {

  override def requires = SystemloaderPlugin

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Debian)(upstartSettings) ++ inConfig(Rpm)(upstartSettings)

  def upstartSettings: Seq[Setting[_]] = Seq(
    // used by other archetypes to define systemloader dependent behaviour
    serverLoading := Some(ServerLoader.Upstart),
    // Upstart settings
    startRunlevels := Some("[2345]"),
    stopRunlevels := Some("[016]"),
    requiredStartFacilities := None,
    requiredStopFacilities := None,
    defaultLinuxStartScriptLocation := "/etc/init",
    killTimeout := 5,
    linuxStartScriptName := Some(packageName.value + ".conf"),
    // add systemloader to mappings
    linuxPackageMappings ++= startScriptMapping(
      linuxStartScriptName.value,
      linuxMakeStartScript.value,
      defaultLinuxStartScriptLocation.value,
      isConf = true
    )
  )

}
