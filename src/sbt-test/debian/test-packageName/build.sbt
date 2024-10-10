enablePlugins(JavaServerAppPackaging)

name := "debian-test"

(Debian / name) := "debian-test-override"

(Linux / packageName) := "debian-test-package"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

Debian / debianPackageDependencies ++= Seq("java2-runtime", "bash (>= 2.05a-11)")

Debian / debianPackageRecommends += "git"

TaskKey[Unit]("checkControlScript") := {
  val script =
    IO.read(target.value / "debian-test-override-0.1.0" / "DEBIAN" / "control")
  assert(script.contains("Package: debian-test-package\n"), "script doesn't [Package: debian-test-package]\n" + script)
  assert(script.contains("Source: debian-test-package\n"), "script doesn't [Source: debian-test-package]\n" + script)
  streams.value.log.success("Successfully tested control script")
  ()
}
