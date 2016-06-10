package com.typesafe.sbt
package packager
package docker

import sbt._

/**
 * Docker settings
 */
trait DockerKeys {
  val dockerGenerateConfig = TaskKey[File]("docker-generate-config", "Generates configuration file for Docker.")
  val dockerPackageMappings = TaskKey[Seq[(File, String)]]("docker-package-mappings", "Generates location mappings for Docker build.")
  val dockerTarget = TaskKey[String]("docker-target", "Defines target used when building and publishing Docker image")

  val dockerBaseImage = SettingKey[String]("dockerBaseImage", "Base image for Dockerfile.")
  val dockerExposedPorts = SettingKey[Seq[Int]]("dockerExposedPorts", "Ports exposed by Docker image")
  val dockerExposedVolumes = SettingKey[Seq[String]]("dockerExposedVolumes", "Volumes exposed by Docker image")
  val dockerRepository = SettingKey[Option[String]]("dockerRepository", "Repository for published Docker image")
  val dockerUpdateLatest = SettingKey[Boolean]("dockerUpdateLatest", "Set to update latest tag")
  val dockerAdditionalVersions = SettingKey[Seq[String]]("dockerAdditionalVersions", "Set additional version tags  such as git branch or git tag")
  val dockerEntrypoint = SettingKey[Seq[String]]("dockerEntrypoint", "Entrypoint arguments passed in exec form")
  val dockerCmd = SettingKey[Seq[String]]("dockerCmd", "Docker CMD. Used together with dockerEntrypoint. Arguments passed in exec form")

  val dockerCommands = TaskKey[Seq[CmdLike]]("dockerCommands", "List of docker commands that form the Dockerfile")
}

