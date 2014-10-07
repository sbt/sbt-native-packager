import com.typesafe.sbt.packager.archetypes.ServerLoader

enablePlugins(JavaServerAppPackaging)

serverLoading in Debian := ServerLoader.Upstart

daemonUser in Debian := "root"

mainClass in Compile := Some("empty")

name := "debian-test"

name in Debian := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

requiredStartFacilities in Debian := Some("[networking]")

requiredStopFacilities in Debian := Some("[networking]")

packageDescription := """A fun package description of our software,
  with multiple lines."""

TaskKey[Unit]("check-startup-script") <<= (target, streams) map { (target, out) =>
  val script = IO.read(target / "debian-test-0.1.0" / "etc" / "init" / "debian-test.conf")
  assert(script.contains("start on runlevel [2345]"), "script doesn't contain start on runlevel header\n" + script)
  assert(script.contains("stop on runlevel [016]"), "script doesn't contain stop on runlevel header\n" + script)
  assert(script.contains("start on started [networking]"), "script contains start on started header\n" + script)
  assert(script.contains("stop on stopping [networking]"), "script contains stop on stopping header\n" + script)
  out.log.success("Successfully tested systemV start up script")
  ()
}