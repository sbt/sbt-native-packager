enablePlugins(JavaServerAppPackaging, JDebPackaging, SystemdPlugin)

// TODO change this after #437 is fixed
daemonUser in Linux := "root"
daemonGroup in Linux := "app-group"

mainClass in Compile := Some("empty")

name := "debian-test"
version := "0.1.0"
maintainer := "Mitch Seymour <mitchseymour@gmail.com>"

packageSummary := "Test debian package"
packageDescription := """A fun package description of our software,
  with multiple lines."""

TaskKey[Unit]("check-startup-script") <<= (target, streams) map { (target, out) =>
  val extracted = target / "tmp" / "extracted-package"
  extracted.mkdirs()
  Seq("dpkg-deb", "-R", (target / "debian-test_0.1.0_all.deb").absolutePath, extracted.absolutePath).!

  val script = IO.read(extracted / "lib" / "systemd" / "system" / "debian-test.service")
  assert(script.startsWith("# right systemd template"), s"override script wasn't picked, script is\n$script")
  ()
}
