enablePlugins(JavaServerAppPackaging)

name := "rpm-test-with-repack"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test rpm package"

packageDescription :=
  """A fun package description of our software,
  with multiple lines."""

rpmRelease := "2"

rpmVendor := "typesafe"

rpmUrl := Some("http://github.com/sbt/sbt-native-packager")

rpmLicense := Some("BSD")

rpmBrpJavaRepackJars := true

TaskKey[Unit]("checkSpecFile") := {
  val spec = IO.read(target.value / "rpm" / "SPECS" / "rpm-test-with-repack.spec")
  assert(!spec.contains("""%define __jar_repack %nil"""), "%pre should not contain jar repack when set to true")
  streams.value.log.success("Successfully tested rpm test file")
  ()
}
