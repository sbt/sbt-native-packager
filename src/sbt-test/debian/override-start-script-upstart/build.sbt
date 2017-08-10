enablePlugins(JavaServerAppPackaging, JDebPackaging, UpstartPlugin)

// TODO change this after #437 is fixed
daemonUser in Linux := "root"
daemonGroup in Linux := "app-group"

mainClass in Compile := Some("empty")

name := "debian-test"
version := "0.1.0"
maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"
packageDescription := """A fun package description of our software,
  with multiple lines."""

TaskKey[Unit]("checkStartupScript") := {
  val extracted = target.value / "tmp" / "extracted-package"
  extracted.mkdirs()
  sys.process
    .Process(Seq("dpkg-deb", "-R", (target.value / "debian-test_0.1.0_all.deb").absolutePath, extracted.absolutePath))
    .!

  val script = IO.read(extracted / "etc" / "init" / "debian-test.conf")
  assert(script.startsWith("# right upstart template"), s"override script wasn't picked, script is\n$script")
  ()
}
