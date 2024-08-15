package com.typesafe.sbt.packager.archetypes.jlink

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import JlinkPlugin.Ignore.byPackagePrefix
import JlinkPlugin.javaVersionPattern
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JlinkSpec extends AnyFlatSpec with Matchers {
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

  "PackageDependency.parse" should "produce proper dependencies" in {
    var line =
      "   akka                                               -> akka                                               akka-actor_2.13-2.6.0.jar"
    JlinkPlugin.PackageDependency.parse(line) should equal(None)

    line =
      "   akka                                               -> akka.actor                                         akka-actor_2.13-2.6.0.jar"
    JlinkPlugin.PackageDependency.parse(line) should equal(
      Some(
        JlinkPlugin
          .PackageDependency("akka", "akka.actor", JlinkPlugin.PackageDependency.JarOrDir("akka-actor_2.13-2.6.0.jar"))
      )
    )

    line =
      "   akka                                               -> java.io                                            java.base"
    JlinkPlugin.PackageDependency.parse(line) should equal(
      Some(JlinkPlugin.PackageDependency("akka", "java.io", JlinkPlugin.PackageDependency.Module("java.base")))
    )

    line =
      "   akka.actor                                         -> sun.misc                                           JDK internal API (jdk.unsupported)"
    JlinkPlugin.PackageDependency.parse(line) should equal(
      Some(
        JlinkPlugin.PackageDependency("akka.actor", "sun.misc", JlinkPlugin.PackageDependency.Module("jdk.unsupported"))
      )
    )

    line =
      "   akka.http.ccompat                                  -> scala.reflect.api                                  not found"
    JlinkPlugin.PackageDependency.parse(line) should equal(
      Some(
        JlinkPlugin.PackageDependency("akka.http.ccompat", "scala.reflect.api", JlinkPlugin.PackageDependency.NotFound)
      )
    )

    line = "   org.test.scala.testproject                    -> org.test.testproject                          classes"
    JlinkPlugin.PackageDependency.parse(line) should equal(
      Some(
        JlinkPlugin.PackageDependency(
          "org.test.scala.testproject",
          "org.test.testproject",
          JlinkPlugin.PackageDependency.Classes
        )
      )
    )

    line = "akka-actor_2.13-2.6.0.jar -> java.base"
    JlinkPlugin.PackageDependency.parse(line) should equal(None)

    line = "akka-actor_2.13-2.6.0.jar -> not found"
    JlinkPlugin.PackageDependency.parse(line) should equal(None)

  }

  "JlinkPlugin.parseJdeps" should "generate correct dependencies" in {
    val input = new String(
      Files.readAllBytes(Paths.get(this.getClass.getResource("/jdeps_output.txt").toURI)),
      StandardCharsets.UTF_8
    );
    val deps = JlinkPlugin.parseJdeps(input)
    deps.size should be > 0
    val m = deps.groupBy(_.source)
    m(JlinkPlugin.PackageDependency.NotFound).size should equal(65)
    m(JlinkPlugin.PackageDependency.Classes).size should equal(6)
    m(JlinkPlugin.PackageDependency.Module("java.base")).size should equal(612)
  }

}
