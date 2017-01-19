
enablePlugins(JavaServerAppPackaging)

name := "rpm-test"

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

TaskKey[Unit]("check-spec-file") <<= (target, streams) map { (target, out) =>
  val spec = IO.read(target / "rpm" / "SPECS" / "rpm-test.spec")
  assert(spec.contains("""%define __jar_repack %nil""", "Missing java repack disabling in %pre"))
  out.log.success("Successfully tested rpm test file")
  ()
}
