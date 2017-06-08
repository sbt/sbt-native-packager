enablePlugins(JavaServerAppPackaging)

name := "rpm-test-no-repack"

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

rpmBrpJavaRepackJars := false

TaskKey[Unit]("check-spec-file") := {
  val spec = IO.read(target.value / "rpm" / "SPECS" / "rpm-test-no-repack.spec")
  assert(spec.contains("""%define __jar_repack %nil"""), "Missing java repack disabling in %pre")
  streams.value.log.success("Successfully tested rpm test file")
  ()
}
