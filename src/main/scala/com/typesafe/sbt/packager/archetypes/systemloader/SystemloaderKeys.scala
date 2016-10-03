package com.typesafe.sbt.packager.archetypes.systemloader

import sbt._

trait SystemloaderKeys {
  val serverLoading = SettingKey[Option[ServerLoader.ServerLoader]](
    "server-loader",
    "Loading system to be used for application start script")
  val serviceAutostart = SettingKey[Boolean](
    "service-autostart",
    "Automatically start the service after installation")
  val startRunlevels = SettingKey[Option[String]](
    "start-runlevels",
    "Sequence of runlevels on which application will start up")
  val stopRunlevels = SettingKey[Option[String]](
    "stop-runlevels",
    "Sequence of runlevels on which application will stop")
  val requiredStartFacilities = SettingKey[Option[String]](
    "required-start-facilities",
    "Names of system services that should be provided at application start")
  val requiredStopFacilities = SettingKey[Option[String]](
    "required-stop-facilities",
    "Names of system services that should be provided at")
  val termTimeout =
    SettingKey[Int]("term-timeout", "Timeout before sigterm on stop")
  val killTimeout = SettingKey[Int](
    "kill-timeout",
    "Timeout before sigkill on stop (after term)")
  val retryTimeout =
    SettingKey[Int]("retry-timeout", "Timeout between retries in seconds")
  val retries =
    SettingKey[Int]("retries", "Number of retries to start service")

}
