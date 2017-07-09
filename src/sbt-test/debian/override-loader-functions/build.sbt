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

TaskKey[Unit]("checkLoaderFunctions") := {
  val extracted = target.value / "tmp" / "extracted-package"
  extracted.mkdirs()
  Seq("dpkg-deb", "-e", (target.value / "debian-test_0.1.0_all.deb").absolutePath, extracted.absolutePath).!

  val script = IO.read(extracted / "postinst")

  assert(script.contains("# right systemd template"), s"override script wasn't picked, script is\n$script")
  assert(!script.contains("wrong systemd start template"), s"script contained wrong template, script is\n$script")
  ()
}
