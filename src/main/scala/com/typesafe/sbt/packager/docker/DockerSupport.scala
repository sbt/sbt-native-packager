package com.typesafe.sbt.packager.docker

object DockerSupport {

  @deprecated
  def chownFlag(version: DockerVersion): Boolean =
    (version.major == 17 && version.minor >= 9) || version.major > 17

  @deprecated
  def multiStage(version: DockerVersion): Boolean =
    (version.major == 17 && version.minor >= 5) || version.major > 17

  def chownFlag(version: DockerVersion, apiVersion: DockerApiVersion): Boolean =
    ((version.major == 17 && version.minor >= 9) || version.major > 17) || (apiVersion.major == 1 && apiVersion.minor >= 32)

  def multiStage(version: DockerVersion, apiVersion: DockerApiVersion): Boolean =
    ((version.major == 17 && version.minor >= 5) || version.major > 17) || (apiVersion.major == 1 && apiVersion.minor >= 29)
}
