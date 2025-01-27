package com.typesafe.sbt.packager.docker

import org.scalatest._
import org.scalatest.diagrams.Diagrams
import org.scalatest.flatspec.AnyFlatSpec

class DockerApiVersionSpec extends AnyFlatSpec with Diagrams {
  "DockerApiVersion" should "parse 1.40" in {
    val v = DockerApiVersion.parse("1.40")
    assert(v == Some(DockerApiVersion(1, 40)))
  }
}
