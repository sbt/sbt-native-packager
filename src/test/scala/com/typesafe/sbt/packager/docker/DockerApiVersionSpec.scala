package com.typesafe.sbt.packager.docker

import org.scalatest._

class DockerApiVersionSpec extends FlatSpec with DiagrammedAssertions {
  "DockerApiVersion" should "parse 1.40" in {
    val v = DockerApiVersion.parse("1.40")
    assert(v == Some(DockerApiVersion(1, 40)))
  }
}
