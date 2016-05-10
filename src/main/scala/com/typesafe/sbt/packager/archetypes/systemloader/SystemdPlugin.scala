package com.typesafe.sbt.packager.archetypes.systemloader

import sbt._
import sbt.Keys.{ target, sourceDirectory }
import com.typesafe.sbt.packager.Keys.{
  maintainerScripts,
  packageName,
  linuxStartScriptName,
  linuxStartScriptTemplate,
  linuxMakeStartScript,
  linuxScriptReplacements,
  linuxPackageMappings,
  defaultLinuxStartScriptLocation
}
import com.typesafe.sbt.packager.archetypes.MaintainerScriptHelper.maintainerScriptsAppend
import com.typesafe.sbt.packager.archetypes.ServerLoader._
import com.typesafe.sbt.packager.archetypes.ServerLoader.Systemd
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.{ Linux }
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.debian.DebianPlugin.autoImport.{ Debian, DebianConstants }
import com.typesafe.sbt.packager.rpm.RpmPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin.autoImport.{ Rpm, RpmConstants }
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.{ Universal }

import java.nio.file.{ Paths, Files }

object SystemdPlugin extends AutoPlugin {

  override def requires = SystemloaderPlugin && DebianPlugin && RpmPlugin

  object autoImport extends Keys {
    // all systemd specific settings/tasks here
    val systemdExitSuccessStatus = settingKey[Int]("defines the ExitSuccessStatus for systemd")
  }

  import autoImport._

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Debian)(systemdSettings) ++ inConfig(Rpm)(systemdSettings)

  def systemdSettings: Seq[Setting[_]] = Seq(
    // used by other archetypes to define systemloader dependent behaviour
    serverLoading := Systemd,
    // Systemd settings
    startRunlevels := None,
    stopRunlevels := None,
    requiredStartFacilities := Some("network.target"),
    requiredStopFacilities := Some("network.target"),
    defaultLinuxStartScriptLocation := "/usr/lib/systemd/system",
    linuxStartScriptName := Some(packageName.value + ".service")
  )


}
