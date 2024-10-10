import com.typesafe.sbt.packager.linux._

enablePlugins(RpmPlugin)

name := "rpm-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test rpm package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

rpmRelease := "1"

rpmVendor := "typesafe"

rpmUrl := Some("http://github.com/sbt/sbt-native-packager")

rpmLicense := Some("BSD")

rpmEpoch := Some(1)

(Rpm / packageArchitecture) := "x86_64"

(Rpm / linuxPackageMappings) := {
  val mapping1 = ((baseDirectory.value / "test"), "tmp/test")
  val mapping2 = ((baseDirectory.value / "build.sbt"), "/tmp/build.sbt")
  Seq(LinuxPackageMapping(Seq(mapping1, mapping2)))
}

(Rpm / defaultLinuxInstallLocation) := "/opt/foo"

TaskKey[Unit]("checkSpecFile") := {
  val spec = IO.read(target.value / "rpm" / "SPECS" / "rpm-test.spec")
  assert(spec contains "Name: rpm-test", "Contains project name")
  assert(spec contains "Version: 0.1.0", "Contains project version")
  assert(spec contains "Release: 1", "Contains project release")
  assert(spec contains "Summary: Test rpm package", "Contains project summary")
  assert(spec contains "License: BSD", "Contains project license")
  assert(spec contains "Epoch: 1", "Contains epoch of 1")
  assert(spec contains "Vendor: typesafe", "Contains project vendor")
  assert(spec contains "URL: http://github.com/sbt/sbt-native-packager", "Contains project url")
  assert(spec contains "BuildArch: x86_64", "Contains project package architecture")

  assert(
    spec contains
      "%description\nA fun package description of our software,\n  with multiple lines.",
    "Contains project description"
  )

  assert(
    spec contains
      "%files\n%attr(755,root,root) /tmp/test\n%attr(755,root,root) /tmp/build.sbt",
    "Contains package mappings"
  )

  streams.value.log.success("Successfully tested rpm test file")
  ()
}
