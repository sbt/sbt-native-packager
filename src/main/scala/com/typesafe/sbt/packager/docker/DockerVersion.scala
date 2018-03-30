package com.typesafe.sbt.packager.docker

import scala.util.matching.Regex

case class DockerVersion(major: Int, minor: Int, patch: Int, release: Option[String])

object DockerVersion {
  private val DockerVersionPattern: Regex = "^'?([0-9]+).([0-9]+).([0-9]+)-?([-a-z0-9]+)?'?$".r

  def parse(version: String): Option[DockerVersion] =
    Option(version).collect {
      case DockerVersionPattern(major, minor, patch, release) =>
        new DockerVersion(major.toInt, minor.toInt, patch.toInt, Option(release))
    }
}
