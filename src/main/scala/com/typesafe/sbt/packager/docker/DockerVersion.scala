package com.typesafe.sbt.packager.docker

import scala.util.matching.Regex

case class DockerVersion(major: Int, minor: Int, patch: Int, release: Option[String])

object DockerVersion {
  private val DockerVersionPattern: Regex = """^'?([0-9]+)\.([0-9]+)(\.[0-9]+)?(\W|_)?(.+)?'?$""".r

  def parse(version: String): Option[DockerVersion] =
    Option(version).collect {
      case DockerVersionPattern(major, minor, patch, _, release) =>
        new DockerVersion(
          major.toInt,
          minor.toInt,
          Option(patch) match {
            case Some(p) => p.drop(1).toInt
            case _       => 0
          },
          Option(release)
        )
    }
}
