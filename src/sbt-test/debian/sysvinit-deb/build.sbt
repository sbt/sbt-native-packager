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

daemonStdoutLogFile := Some("test.log")

TaskKey[Unit]("checkControlFiles") := {
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

TaskKey[Unit]("checkStartupScript") := {
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
  assert(
    script.contains(
      """start-stop-daemon --background --chdir /usr/share/debian-test --chuid "$DAEMON_USER" --make-pidfile --pidfile "$PIDFILE" --startas /bin/sh --start -- -c "exec $RUN_CMD $RUN_OPTS ${stdout_redirect}"""
    ),
    "script has wrong startup line\n" + script
  )
  assert(script.contains("""logfile="test.log""""), "script contains wrong log file for stdout\n" + script)

  streams.value.log.success("Successfully tested systemV start up script")
  ()
}

TaskKey[Unit]("checkAutostart") := {
  val script = IO.read(target.value / "debian-test-0.1.0" / "DEBIAN" / "postinst")
  assert(script.contains("""addService debian-test || echo "debian-test could not be registered"
      |startService debian-test || echo "debian-test could not be started"
      |""".stripMargin), "addService, startService post install commands missing or incorrect")
  ()
}

TaskKey[Unit]("checkNoAutostart") := {
  val script = IO.read(target.value / "debian-test-0.1.0" / "DEBIAN" / "postinst")
  assert(script.contains("""addService debian-test || echo "debian-test could not be registered"
      |""".stripMargin), "addService post install commands missing or incorrect")
  ()
}
