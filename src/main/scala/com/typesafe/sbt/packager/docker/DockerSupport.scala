package com.typesafe.sbt.packager.docker

object DockerSupport {

  def chownFlag(version: DockerVersion): Boolean =
    (version.major == 17 && version.minor >= 9) || version.major > 17

}
