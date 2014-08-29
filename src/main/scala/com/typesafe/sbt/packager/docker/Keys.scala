package com.typesafe.sbt
package packager
package docker

import sbt._

trait DockerKeys {
  val dockerGenerateConfig = TaskKey[File]("docker-generate-config", "Generates configuration file for Docker.")
  val dockerGenerateContext = TaskKey[File]("docker-generate-context", "Generates context directory for Docker.")
  val dockerPackageMappings = TaskKey[Seq[(File, String)]]("docker-package-mappings", "Generates location mappings for Docker build.")
  val dockerTarget = TaskKey[String]("docker-target", "Defines target used when building and publishing Docker image")

  val dockerBaseImage = SettingKey[String]("dockerBaseImage", "Base image for Dockerfile.")
  val dockerExposedPorts = SettingKey[Seq[Int]]("dockerExposedPorts", "Ports exposed by Docker image")
  val dockerExposedVolumes = SettingKey[Seq[String]]("dockerExposedVolumes", "Volumes exposed by Docker image")
  val dockerRepository = SettingKey[Option[String]]("dockerRepository", "Repository for published Docker image")
  val dockerCmd = SettingKey[Seq[String]]("dockerCmd", "Commands which will be put into the CMD command")
}

object Keys extends DockerKeys {
  def cacheDirectory = sbt.Keys.cacheDirectory
  def mappings = sbt.Keys.mappings
  def name = sbt.Keys.name
  def packageName = universal.Keys.packageName
  def executableScriptName = universal.Keys.executableScriptName
  def stage = universal.Keys.stage
  def publish = sbt.Keys.publish
  def publishArtifact = sbt.Keys.publishArtifact
  def publishLocal = sbt.Keys.publishLocal
  def sourceDirectory = sbt.Keys.sourceDirectory
  def streams = sbt.Keys.streams
  def target = sbt.Keys.target
  def version = sbt.Keys.version
  def defaultLinuxInstallLocation = packager.Keys.defaultLinuxInstallLocation
  def daemonUser = linux.Keys.daemonUser
  def maintainer = linux.Keys.maintainer
}
