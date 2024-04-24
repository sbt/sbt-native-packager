package com.typesafe.sbt.packager.docker

import scala.util.matching.Regex

case class DockerApiVersion(major: Int, minor: Int)

object DockerApiVersion {
  private val DockerApiVersionPattern: Regex = """^'?([0-9]+)\.([0-9]+)'?$""".r

  def parse(version: String): Option[DockerApiVersion] =
    Option(version).collect { case DockerApiVersionPattern(major, minor) =>
      new DockerApiVersion(major.toInt, minor.toInt)
    }
}
