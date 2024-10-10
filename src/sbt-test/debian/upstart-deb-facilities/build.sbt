enablePlugins(JavaServerAppPackaging, UpstartPlugin)

(Debian / daemonUser) := "root"

(Compile / mainClass) := Some("empty")

name := "debian-test"

(Debian / name) := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

(Debian / requiredStartFacilities) := Some("[networking]")

(Debian / requiredStopFacilities) := Some("[networking]")

packageDescription := """A fun package description of our software,
  with multiple lines."""

TaskKey[Unit]("checkStartupScript") := {
  val script = IO.read(target.value / "debian-test-0.1.0" / "etc" / "init" / "debian-test.conf")
  assert(script.contains("start on runlevel [2345]"), "script doesn't contain start on runlevel header\n" + script)
  assert(script.contains("stop on runlevel [016]"), "script doesn't contain stop on runlevel header\n" + script)
  assert(script.contains("start on started [networking]"), "script contains start on started header\n" + script)
  assert(script.contains("stop on stopping [networking]"), "script contains stop on stopping header\n" + script)
  assert(script.contains("kill timeout 5"), "script doenst't contain kill 'kill timeout 5'\n" + script)
  streams.value.log.success("Successfully tested systemV start up script")
  ()
}
