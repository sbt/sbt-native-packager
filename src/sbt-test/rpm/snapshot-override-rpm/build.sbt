enablePlugins(RpmPlugin)

name := "rpm-test"

version := "0.1.0-SNAPSHOT"

maintainer := "Keir Lawson <keirlawson@gmail.com>"

packageSummary := "Snapshot test rpm package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

rpmVendor := "typesafe"

rpmUrl := Some("http://github.com/sbt/sbt-native-packager")

rpmLicense := Some("BSD")

Rpm / version := "1"

TaskKey[Unit]("checkSnapshot") := {
  assert(rpmRelease.value == "SNAPSHOT", s"RPM has incorrect value ${rpmRelease.value}")
  assert(rpmMetadata.value.version == "1", s"RPM has incorrect value ${rpmMetadata.value.version}")
  assert((Rpm / version).value == "1", s"RPM has incorrect value ${(Rpm / version).value}")
}
