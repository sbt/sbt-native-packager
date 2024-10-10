enablePlugins(JavaServerAppPackaging, JDebPackaging, SystemVPlugin)

// TODO change this after #437 is fixed
(Linux / daemonUser) := "root"
(Linux / daemonGroup) := "app-group"

(Compile / mainClass) := Some("empty")

name := "debian-test"
version := "0.1.0"
maintainer := "Mitch Seymour <mitchseymour@gmail.com>"

packageSummary := "Test debian package"
packageDescription := """A fun package description of our software,
  with multiple lines."""

TaskKey[Unit]("checkStartupScript") := {
  val extracted = target.value / "tmp" / "extracted-package"
  extracted.mkdirs()
  sys.process
    .Process(Seq("dpkg-deb", "-R", (target.value / "debian-test_0.1.0_all.deb").absolutePath, extracted.absolutePath))
    .!

  val script = IO.read(extracted / "etc" / "init.d" / "debian-test")
  assert(script.startsWith("# right systemv template"), s"override script wasn't picked, script is\n$script")
  ()
}
