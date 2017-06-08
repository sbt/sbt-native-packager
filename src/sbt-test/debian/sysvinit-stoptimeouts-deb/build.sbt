enablePlugins(JavaServerAppPackaging, SystemVPlugin)

daemonUser in Debian := "root"

mainClass in Compile := Some("empty")

name := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

requiredStartFacilities := Some("$test-service")

requiredStartFacilities in Debian := Some("$test-deb-service")

termTimeout in Debian := 10

killTimeout in Debian := 20

TaskKey[Unit]("check-control-files") := {
  val header = "#!/bin/sh"
  val debian = target.value / "debian-test-0.1.0" / "DEBIAN"
  val postinst = IO.read(debian / "postinst")
  val postrm = IO.read(debian / "postrm")
  Seq(postinst, postrm) foreach { script =>
    assert(script.startsWith(header), "script doesn't start with #!/bin/sh header:\n" + script)
    assert(header.r.findAllIn(script).length == 1, "script contains more than one header line:\n" + script)
  }
  streams.value.log.success("Successfully tested systemV control files")
  ()
}

TaskKey[Unit]("check-startup-script") := {
  val script =
    IO.read(target.value / "debian-test-0.1.0" / "etc" / "init.d" / "debian-test")
  assert(script.contains("# Default-Start: 2 3 4 5"), "script doesn't contain Default-Start header\n" + script)
  assert(script.contains("# Default-Stop: 0 1 6"), "script doesn't contain Default-Stop header\n" + script)
  assert(
    script.contains("# Required-Start: $test-deb-service"),
    "script doesn't contain Required-Start header\n" + script
  )
  assert(
    script.contains("# Required-Stop: $remote_fs $syslog"),
    "script doesn't contain Required-Stop header\n" + script
  )
  assert(script.contains("--retry=TERM/10/KILL/20"), "script doesn't contains stop timeouts\n" + script)
  streams.value.log.success("Successfully tested systemV start up script")
  ()
}
