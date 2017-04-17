package com.typesafe.sbt
package packager
package docker

import sbt._

/**
  * Docker settings
  */
trait DockerKeys {
  val dockerGenerateConfig = TaskKey[File]("docker-generate-config", "Generates configuration file for Docker.")
  val dockerPackageMappings =
    TaskKey[Seq[(File, String)]]("docker-package-mappings", "Generates location mappings for Docker build.")

  val dockerBaseImage =
    SettingKey[String]("dockerBaseImage", "Base image for Dockerfile.")
  val dockerExposedPorts = SettingKey[Seq[Int]]("dockerExposedPorts", "TCP Ports exposed by Docker image")
  val dockerExposedUdpPorts = SettingKey[Seq[Int]]("dockerExposedUdpPorts", "UDP Ports exposed by Docker image")
  val dockerExposedVolumes = SettingKey[Seq[String]]("dockerExposedVolumes", "Volumes exposed by Docker image")
  val dockerRepository = SettingKey[Option[String]]("dockerRepository", "Repository for published Docker image")
  val dockerAlias =
    SettingKey[DockerAlias]("dockerAlias", "Docker alias for the built image")
  val dockerUpdateLatest =
    SettingKey[Boolean]("dockerUpdateLatest", "Set to update latest tag")
  val dockerEntrypoint = SettingKey[Seq[String]]("dockerEntrypoint", "Entrypoint arguments passed in exec form")
  val dockerCmd = SettingKey[Seq[String]](
    "dockerCmd",
    "Docker CMD. Used together with dockerEntrypoint. Arguments passed in exec form"
  )
  val dockerExecCommand = SettingKey[Seq[String]]("dockerExecCommand", "The shell command used to exec Docker")
  val dockerBuildOptions = SettingKey[Seq[String]]("dockerBuildOptions", "Options used for the Docker build")
  val dockerBuildCommand = SettingKey[Seq[String]]("dockerBuildCommand", "Command for building the Docker image")
  val dockerLabels = SettingKey[Map[String, String]]("dockerLabels", "Labels applied to the Docker image")
  val dockerRmiCommand =
    SettingKey[Seq[String]]("dockerRmiCommand", "Command for removing the Docker image from the local registry")

  val dockerCommands = TaskKey[Seq[CmdLike]]("dockerCommands", "List of docker commands that form the Dockerfile")
}
