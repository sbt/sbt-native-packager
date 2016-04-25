package com.typesafe.sbt.packager.archetypes

import sbt._
import ServerLoader._

// we should put this setting in a more appropriate place
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.serverLoading

object SystemD extends AutoPlugin {

  object autoImport {
    // all systemd specific settings/tasks here
    val systemdExitSuccessStatus = settingKey[Int]("defines the ExitSuccessStatus for systemd")
  }

  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    // keep legacy code working
    serverLoading := Systemd,
    // sets specific systemd settings
    systemdExitSuccessStatus := 0
  )
}
