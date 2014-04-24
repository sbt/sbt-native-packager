package com.typesafe.sbt
package packager
package docker

import sbt._

trait DockerKeys {
  val dockerGenerateConfig = TaskKey[File]("docker-generate-config", "Generates configuration file for Docker.")
  val dockerBaseImage = SettingKey[String]("dockerBaseImage", "Base image for Dockerfile.")
  val dockerBaseDirectory = SettingKey[String]("dockerBaseDirectory", "Base directory in Docker image under which to place files.")
}

object Keys extends DockerKeys {
  def mappings = sbt.Keys.mappings
  def daemonUser = linux.Keys.daemonUser
  def maintainer = linux.Keys.maintainer
  def normalizedName = universal.Keys.normalizedName
  def stage = universal.Keys.stage
  def stagingDirectory = universal.Keys.stagingDirectory
  def target = sbt.Keys.target
  def streams = sbt.Keys.streams
}
