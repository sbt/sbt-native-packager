package com.typesafe.sbt.packager.docker

import org.scalatest._

class DockerVersionSpec extends FlatSpec with DiagrammedAssertions {
  "DockerVersion" should "parse 18.09.2" in {
    val v = DockerVersion.parse("18.09.2")
    assert(v == Some(DockerVersion(18, 9, 2, None)))
  }

  it should "parse 18.06.1-ce" in {
    val v = DockerVersion.parse("18.06.1-ce")
    assert(v == Some(DockerVersion(18, 6, 1, Some("ce"))))
  }

  it should "parse 18.03.1-ee-8" in {
    val v = DockerVersion.parse("18.03.1-ee-8")
    assert(v == Some(DockerVersion(18, 3, 1, Some("ee-8"))))
  }

  it should "parse 18.09.ee.2-1.el7.rhel" in {
    val v = DockerVersion.parse("18.09.ee.2-1.el7.rhel")
    assert(v == Some(DockerVersion(18, 9, 0, Some("ee.2-1.el7.rhel"))))
  }

  it should "parse 17.05.0~ce-0ubuntu-xenial" in {
    val v = DockerVersion.parse("17.05.0~ce-0ubuntu-xenial")
    assert(v == Some(DockerVersion(17, 5, 0, Some("ce-0ubuntu-xenial"))))
  }
}
