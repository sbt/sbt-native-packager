enablePlugins(JavaServerAppPackaging, JDebPackaging)

name := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

Debian / debianPackageDependencies := Seq()

TaskKey[Unit]("checkDependencies") := {
  val extracted = target.value / "extracted"
  sys.process
    .Process(Seq("dpkg-deb", "-R", (target.value / "debian-test_0.1.0_all.deb").absolutePath, extracted.absolutePath))
    .!

  val control = IO.read(extracted / "DEBIAN" / "control")
  assert(!control.contains("Depends:"))

  streams.value.log.success("Successfully tested systemV control files")
  ()
}
