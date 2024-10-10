enablePlugins(JavaServerAppPackaging, UpstartPlugin)

name := "debian-test"

executableScriptName := "debian-exec"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

(Debian / debianPackageDependencies) ++= Seq("java2-runtime", "bash (>= 2.05a-11)")

(Debian / debianPackageRecommends) += "git"

TaskKey[Unit]("checkUpstartScript") := {
  val script = IO.read(target.value / "debian-test-0.1.0" / "etc" / "init" / "debian-test.conf")
  assert(script.contains("exec sudo -E -u debian-test bin/debian-exec"), "wrong exec call\n" + script)
  streams.value.log.success("Successfully tested control script")
  ()
}
