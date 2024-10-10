enablePlugins(JavaServerAppPackaging, JDebPackaging, UpstartPlugin)

// TODO change this after #437 is fixed
(Linux / daemonUser) := "root"

(Linux / daemonGroup) := "app-group"

(Compile / mainClass) := Some("empty")

name := "debian-test"

(Debian / name) := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

TaskKey[Unit]("checkEtcDefault") := {
  val extracted = target.value / "tmp" / "extracted-package"
  extracted.mkdirs()
  sys.process
    .Process(Seq("dpkg-deb", "-R", (target.value / "debian-test_0.1.0_all.deb").absolutePath, extracted.absolutePath))
    .!

  val script = IO.read(extracted / "etc" / "default" / "debian-test")
  assert(
    script.startsWith("# right etc-default template"),
    s"etc-default script wasn't picked, contents instead are:\n$script"
  )
  ()
}
