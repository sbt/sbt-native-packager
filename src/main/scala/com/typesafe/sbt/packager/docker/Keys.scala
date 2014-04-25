package com.typesafe.sbt
package packager
package docker

import sbt._

trait DockerKeys {
  val dockerGenerateConfig = TaskKey[File]("docker-generate-config", "Generates configuration file for Docker.")
  val dockerGenerateContext = TaskKey[Unit]("docker-generate-context", "Generates context directory for Docker.")
  val dockerPackageMappings = TaskKey[Seq[(File, String)]]("docker-package-mappings", "Generates location mappings for Docker build.")
  val dockerBaseImage = SettingKey[String]("dockerBaseImage", "Base image for Dockerfile.")
  val defaultDockerInstallLocation = SettingKey[String]("defaultDockerInstallLocation", "The location where we will install Docker packages.")
}

object Keys extends DockerKeys {
  def mappings = sbt.Keys.mappings
  def sourceDirectory = sbt.Keys.sourceDirectory
  def target = sbt.Keys.target
  def normalizedName = universal.Keys.normalizedName
  def daemonUser = linux.Keys.daemonUser
  def maintainer = linux.Keys.maintainer
}
