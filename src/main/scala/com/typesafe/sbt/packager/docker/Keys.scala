package com.typesafe.sbt
package packager
package docker

import sbt._

/**
  * Docker settings
  */
@deprecated("Internal use only. Please don't extend this trait", "1.3.15")
trait DockerKeys {
  @transient
  val dockerGenerateConfig = TaskKey[File]("docker-generate-config", "Generates configuration file for Docker.")
  @transient
  val dockerPackageMappings =
    taskKey[Seq[(PluginCompat.FileRef, String)]]("Generates location mappings for Docker build.")

  val dockerBaseImage =
    SettingKey[String]("dockerBaseImage", "Base image for Dockerfile.")
  val dockerExposedPorts = SettingKey[Seq[Int]]("dockerExposedPorts", "TCP Ports exposed by Docker image")
  val dockerExposedUdpPorts = SettingKey[Seq[Int]]("dockerExposedUdpPorts", "UDP Ports exposed by Docker image")
  val dockerExposedVolumes = SettingKey[Seq[String]]("dockerExposedVolumes", "Volumes exposed by Docker image")
  val dockerRepository = SettingKey[Option[String]]("dockerRepository", "Repository for published Docker image")
  val dockerUsername = SettingKey[Option[String]]("dockerUsername", "Username for published Docker image")
  val dockerAlias =
    SettingKey[DockerAlias]("dockerAlias", "Docker alias for the built image")
  val dockerAliases =
    SettingKey[Seq[DockerAlias]]("dockerAliases", "Docker aliases for the built image")
  val dockerUpdateLatest =
    SettingKey[Boolean]("dockerUpdateLatest", "Set to update latest tag")
  val dockerAutoremoveMultiStageIntermediateImages =
    SettingKey[Boolean](
      "dockerAutoremoveMultiStageIntermediateImages",
      "Automatically remove multi-stage intermediate images"
    )
  val dockerEntrypoint = SettingKey[Seq[String]]("dockerEntrypoint", "Entrypoint arguments passed in exec form")
  val dockerCmd = SettingKey[Seq[String]](
    "dockerCmd",
    "Docker CMD. Used together with dockerEntrypoint. Arguments passed in exec form"
  )
  val dockerExecCommand = SettingKey[Seq[String]]("dockerExecCommand", "The shell command used to exec Docker")
  @transient
  val dockerVersion = TaskKey[Option[DockerVersion]]("dockerVersion", "The docker server version")
  val dockerBuildOptions = SettingKey[Seq[String]]("dockerBuildOptions", "Options used for the Docker build")
  val dockerBuildEnvVars =
    SettingKey[Map[String, String]]("dockerBuildEnvVars", "Environment variables used for the Docker build")
  val dockerBuildCommand = SettingKey[Seq[String]]("dockerBuildCommand", "Command for building the Docker image")
  val dockerLabels = SettingKey[Map[String, String]]("dockerLabels", "Labels applied to the Docker image")
  val dockerEnvVars =
    SettingKey[Map[String, String]]("dockerEnvVars", "Environment Variables applied to the Docker image")
  val dockerRmiCommand =
    SettingKey[Seq[String]]("dockerRmiCommand", "Command for removing the Docker image from the local registry")

  @transient
  val dockerCommands = TaskKey[Seq[CmdLike]]("dockerCommands", "List of docker commands that form the Dockerfile")
}

// Workaround to pass mima.
// In the next version bump we should hide DockerKeys trait to package private.
private[packager] trait DockerKeysEx extends DockerKeys {
  lazy val dockerPermissionStrategy = settingKey[DockerPermissionStrategy]("The strategy to change file permissions.")
  lazy val dockerChmodType = settingKey[DockerChmodType]("The file permissions for the files copied into Docker image.")
  @transient
  lazy val dockerAdditionalPermissions =
    taskKey[Seq[(DockerChmodType, String)]]("Explicit chmod calls to some of the paths.")
  @transient
  val dockerApiVersion = TaskKey[Option[DockerApiVersion]]("dockerApiVersion", "The docker server api version")
  @deprecated("Use dockerGroupLayers instead", "1.7.1")
  val dockerLayerGrouping = settingKey[String => Option[Int]](
    "Group files by path into in layers to increase docker cache hits. " +
      "Lower index means the file would be a part of an earlier layer."
  )
  @transient
  val dockerGroupLayers = taskKey[PartialFunction[(PluginCompat.FileRef, String), Int]](
    "Group files by mapping into layers to increase docker cache hits. " +
      "Lower index means the file would be a part of an earlier layer."
  )
  @transient
  val dockerLayerMappings =
    taskKey[Seq[LayeredMapping]]("List of layer, source file and destination in Docker image.")
  val dockerBuildInit = SettingKey[Boolean](
    "dockerBuildInit",
    "Whether the --init flag should be passed to Docker when building. " +
      "Setting to true will cause Docker to bundle a tini in the container, to run as the init process, which is recommended for JVM apps. " +
      "Requires Docker API version 1.25+"
  )
  @transient
  val dockerBuildkitEnabled = TaskKey[Boolean]("dockerBuildkitEnabled", "Detects whether buildkit is enabled")
  val dockerBuildxPlatforms =
    SettingKey[Seq[String]]("dockerBuildxPlatforms", "The docker image platforms for buildx multi-platform build")
}
