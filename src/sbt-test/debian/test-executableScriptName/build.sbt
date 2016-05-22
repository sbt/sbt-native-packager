enablePlugins(JavaServerAppPackaging, UpstartPlugin)

name := "debian-test"

executableScriptName := "debian-exec"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

debianPackageDependencies in Debian ++= Seq("java2-runtime", "bash (>= 2.05a-11)")

debianPackageRecommends in Debian += "git"

TaskKey[Unit]("check-upstart-script") <<= (target, streams) map { (target, out) =>
  val script = IO.read(target / "debian-test-0.1.0" / "etc" / "init" / "debian-test.conf")
  assert(script.contains("exec sudo -u debian-test bin/debian-exec"), "wrong exec call\n" + script)
  out.log.success("Successfully tested control script")
  ()
}
