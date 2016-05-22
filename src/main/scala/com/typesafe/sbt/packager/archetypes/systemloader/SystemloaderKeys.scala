package com.typesafe.sbt.packager.archetypes.systemloader

import sbt._

trait SystemloaderKeys {
  val serverLoading = SettingKey[Option[ServerLoader.ServerLoader]]("server-loader", "Loading system to be used for application start script")
  val startRunlevels = SettingKey[Option[String]]("start-runlevels", "Sequence of runlevels on which application will start up")
  val stopRunlevels = SettingKey[Option[String]]("stop-runlevels", "Sequence of runlevels on which application will stop")
  val requiredStartFacilities = SettingKey[Option[String]]("required-start-facilities", "Names of system services that should be provided at application start")
  val requiredStopFacilities = SettingKey[Option[String]]("required-stop-facilities", "Names of system services that should be provided at")
}