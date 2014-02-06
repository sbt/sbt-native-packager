import NativePackagerKeys._

packagerSettings

mapGenericFilesToLinux

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

rpmPre := Some("""echo "pre-install"""")

rpmPost := Some("""echo "post-install"""")

rpmPretrans := Some("""echo "pretrans"""")

rpmPosttrans := Some("""echo "posttrans"""")

rpmPreun := Some("""echo "pre-uninstall"""")

rpmPostun := Some("""echo "post-uninstall"""")

TaskKey[Unit]("check-spec-file") <<= (target, streams) map { (target, out) =>
  val spec = IO.read(target / "rpm" / "SPECS" / "rpm-test.spec")
  assert(spec contains "%pre\necho \"pre-install\"", "Spec doesn't contain %pre scriptlet")
  assert(spec contains "%post\necho \"post-install\"", "Spec doesn't contain %post scriptlet")
  assert(spec contains "%pretrans\necho \"pretrans\"", "Spec doesn't contain %pretrans scriptlet")
  assert(spec contains "%posttrans\necho \"posttrans\"", "Spec doesn't contain %posttrans scriptlet")
  assert(spec contains "%preun\necho \"pre-uninstall\"", "Spec doesn't contain %preun scriptlet")
  assert(spec contains "%postun\necho \"post-uninstall\"", "Spec doesn't contain %postun scriptlet")
  out.log.success("Successfully tested rpm test file")
  ()
}

TaskKey[Unit]("check-rpm-version") <<= (target, streams) map { (target, out) =>
  val fullRpmVersion = Process("rpm", Seq("--version")) !! 
  val firstDigit = fullRpmVersion indexWhere Character.isDigit
  val rpmVersion = fullRpmVersion substring firstDigit
  out.log.info("Found rpmVersion: " + rpmVersion)
  val (major, minor, patch) = rpmVersion.trim.split("\\.").map(_.toInt) match {
    case Array(major) => (major, 0, 0)
    case Array(major, minor) => (major, minor, 0)
    case Array(major, minor, patch, _*) => (major, minor, patch)
  }
  assert(major >= 4, "RPM version must be greater than than 4.x.x. Is " + fullRpmVersion)
  ()
}

