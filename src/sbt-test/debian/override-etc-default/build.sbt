enablePlugins(JavaServerAppPackaging, JDebPackaging, UpstartPlugin)

// TODO change this after #437 is fixed
daemonUser in Linux := "root"

daemonGroup in Linux := "app-group"

mainClass in Compile := Some("empty")

name := "debian-test"

name in Debian := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

TaskKey[Unit]("check-etc-default") <<= (target, streams) map { (target, out) =>
  val extracted = target / "tmp" / "extracted-package"
  extracted.mkdirs()
  Seq("dpkg-deb", "-R", (target / "debian-test_0.1.0_all.deb").absolutePath, extracted.absolutePath).!

  val script = IO.read(extracted / "etc" / "default" / "debian-test")
  assert(script.startsWith("# right etc-default template"), s"etc-default script wasn't picked, contents instead are:\n$script")
  ()
}
