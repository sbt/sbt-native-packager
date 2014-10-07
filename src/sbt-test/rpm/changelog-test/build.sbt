enablePlugins(JavaServerAppPackaging)

name := "rpm-test"

version := "0.1.0"

maintainer := "Endrigo Antonini <eantonini@eidoscode.com>"

packageSummary := "Test rpm package with changelog"

packageDescription := """A fun package description of our software,
  with multiple lines."""

rpmRelease := "1"

rpmVendor := "eidoscode"

rpmUrl := Some("http://github.com/sbt/sbt-native-packager")

rpmLicense := Some("BSD")

rpmChangelogFile := Some("conf/changelog")

TaskKey[Unit]("check-spec-file") <<= (target, streams) map { (target, out) =>
  val spec = IO.read(target / "rpm" / "SPECS" / "rpm-test.spec")
  // Check if the RPM writted the changelog tag on the task
  assert(spec contains "%changelog\n", "Spec doesn't contain %changelog tag on the SPEC")
  out.log.success("Successfully tested rpm test file")
  ()
}

