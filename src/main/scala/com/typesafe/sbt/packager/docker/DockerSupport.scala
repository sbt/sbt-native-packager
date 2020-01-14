package com.typesafe.sbt.packager.docker

object DockerSupport {

  def chownFlag(version: DockerVersion, apiVersion: DockerApiVersion): Boolean =
    ((version.major == 17 && version.minor >= 9) || version.major > 17) || (apiVersion.major == 1 && apiVersion.minor >= 32)

  def multiStage(version: DockerVersion, apiVersion: DockerApiVersion): Boolean =
    ((version.major == 17 && version.minor >= 5) || version.major > 17) || (apiVersion.major == 1 && apiVersion.minor >= 29)
}
