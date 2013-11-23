import NativePackagerKeys._
import com.typesafe.sbt.packager.archetypes.ServerLoader

packageArchetype.java_server

serverLoading in Debian := ServerLoader.SystemV

daemonUser in Debian := "root"

mainClass in Compile := Some("empty")

name := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

