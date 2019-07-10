package com.typesafe.sbt.packager.archetypes.jlink

import org.scalatest.{FlatSpec, Matchers}
import JlinkPlugin.Ignore.byPackagePrefix
import JlinkPlugin.javaVersionPattern

class JlinkSpec extends FlatSpec with Matchers {
  "Ignore.byPackagePrefix()" should "match as expected for sample examples" in {
    byPackagePrefix("" -> "")("foo" -> "bar") should be(true)

    byPackagePrefix("foo" -> "bar")("foo" -> "bar") should be(true)
    byPackagePrefix("foo" -> "bar")("bar" -> "foo") should be(false)
    byPackagePrefix("foo" -> "bar")("baz" -> "bar") should be(false)
    byPackagePrefix("foo" -> "bar")("foo" -> "baz") should be(false)

    byPackagePrefix("foo" -> "bar")("foobaz" -> "barqux") should be(false)
    byPackagePrefix("foo" -> "bar")("foo.baz" -> "bar.qux") should be(true)
    byPackagePrefix("foo.baz" -> "bar.qux")("foo" -> "bar") should be(false)

    byPackagePrefix("" -> "", "foo" -> "bar")("baz" -> "qux") should be(true)
    byPackagePrefix("foo" -> "bar", "" -> "")("baz" -> "qux") should be(true)
    byPackagePrefix("foo" -> "", "" -> "bar")("baz" -> "qux") should be(false)
  }

  "javaVersionPattern" should "match known examples" in {
    """JAVA_VERSION="11.0.3"""" should fullyMatch regex (javaVersionPattern withGroup "11")
    // Haven't seen this in the wild, but JEP220 has this example, so we might
    // as well handle it.
    """JAVA_VERSION="1.9.0"""" should fullyMatch regex (javaVersionPattern withGroup "9")
  }
}
