import com.typesafe.sbt.packager.archetypes.ServerLoader

enablePlugins(JavaServerAppPackaging)

serverLoading in Debian := ServerLoader.Systemd

name := "debian-test"

version := "0.1.0"

maintainer := "Alexey Kardapoltsev <alexey.kardapoltsev@frumatic.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

requiredStartFacilities in Debian := Some("network.target")

TaskKey[Unit]("check-startup-script") <<= (target, streams) map { (target, out) =>
  val script = IO.read(target / "debian-test-0.1.0" / "usr" / "lib" / "systemd" / "system" / "debian-test.service")
  assert(script.contains("Requires=network.target"), "script doesn't contain Default-Start header\n" + script)
  out.log.success("Successfully tested systemd start up script")
  ()
}
