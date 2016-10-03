enablePlugins(JavaServerAppPackaging, JDebPackaging)

name := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

debianPackageConflicts in Debian := Seq("debian-test-package")

TaskKey[Unit]("check-conflicts") <<= (target, streams) map { (target, out) =>
  val extracted = target / "extracted"
  Seq("dpkg-deb",
      "-R",
      (target / "debian-test_0.1.0_all.deb").absolutePath,
      extracted.absolutePath).!

  val control = IO.read(extracted / "DEBIAN" / "control")
  assert(control.contains("Conflicts:"))

  out.log.success("Successfully tested systemV control files")
  ()
}
