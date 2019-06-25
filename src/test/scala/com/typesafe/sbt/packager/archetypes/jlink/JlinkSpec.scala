package com.typesafe.sbt.packager.archetypes.jlink

import org.scalatest.{FlatSpec, Matchers}
import JlinkPlugin.Ignore.byPackagePrefix

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
}
