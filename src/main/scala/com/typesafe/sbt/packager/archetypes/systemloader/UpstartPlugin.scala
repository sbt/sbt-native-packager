package com.typesafe.sbt.packager.archetypes.systemloader

import sbt._
import sbt.Keys.{ target, sourceDirectory }
import com.typesafe.sbt.packager.Keys.{
  serverLoading,
  linuxStartScriptTemplate,
  linuxMakeStartScript,
  defaultLinuxStartScriptLocation,
  requiredStartFacilities,
  requiredStopFacilities,
  startRunlevels,
  stopRunlevels
}
import com.typesafe.sbt.packager.archetypes.ServerLoader._
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.debian.DebianPlugin.autoImport.Debian
import com.typesafe.sbt.packager.rpm.RpmPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin.autoImport.Rpm

import java.nio.file.{ Paths, Files }

object UpstartPlugin extends AutoPlugin {

  override def requires = SystemloaderPlugin && DebianPlugin && RpmPlugin

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Debian)(upstartSettings) ++ inConfig(Rpm)(upstartSettings)

  def upstartSettings: Seq[Setting[_]] = Seq(
    // used by other archetypes to define systemloader dependent behaviour
    serverLoading := Upstart,
    // Systemd settings
    startRunlevels := Some("[2345]"),
    stopRunlevels := Some("[016]"),
    requiredStartFacilities := None,
    requiredStopFacilities := None,
    defaultLinuxStartScriptLocation := "/etc/init/"
  )


}
