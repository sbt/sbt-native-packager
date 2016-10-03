enablePlugins(JavaServerAppPackaging, SystemdPlugin)

name := "debian-test"
version := "0.1.0"
maintainer := "Alexey Kardapoltsev <alexey.kardapoltsev@frumatic.com>"

packageSummary := "Test debian package"
packageDescription := """A fun package description of our software,
  with multiple lines."""

requiredStartFacilities in Debian := Some("network.target")

daemonUser in Linux := "testuser"

systemdSuccessExitStatus in Debian += "1"

TaskKey[Unit]("check-startup-script") <<= (target, streams) map {
  (target, out) =>
    val script = IO.read(
      target / "debian-test-0.1.0" / "lib" / "systemd" / "system" / "debian-test.service")
    assert(script.contains("Requires=network.target"),
           "script doesn't contain Default-Start header\n" + script)
    assert(script.contains("User=testuser"),
           "script doesn't contain `User` header\n" + script)
    assert(script.contains("EnvironmentFile=/etc/default/debian-test"),
           "script doesn't contain EnvironmentFile header\n" + script)
    assert(script.contains("SuccessExitStatus=1"),
           "script doesn't contain SuccessExitStatus header\n" + script)
    out.log.success("Successfully tested systemd start up script")
    ()
}

TaskKey[Unit]("check-etc-default") <<= (target, streams) map { (target, out) =>
  val script =
    IO.read(target / "debian-test-0.1.0" / "etc" / "default" / "debian-test")
  assert(
    script.contains("systemd"),
    s"systemd etc-default template wasn't selected; contents are:\n" + script)
  ()
}

TaskKey[Unit]("check-autostart") <<= (target, streams) map { (target, out) =>
  val script = IO.read(target / "debian-test-0.1.0" / "DEBIAN" / "postinst")
  assert(
    script.contains(
      """addService debian-test || echo "debian-test could not be registered"
      |startService debian-test || echo "debian-test could not be started"
      |""".stripMargin),
    "addService, startService post install commands missing or incorrect")
  ()
}

TaskKey[Unit]("check-no-autostart") <<= (target, streams) map {
  (target, out) =>
    val script = IO.read(target / "debian-test-0.1.0" / "DEBIAN" / "postinst")
    assert(
      script.contains(
        """addService debian-test || echo "debian-test could not be registered"
      |""".stripMargin),
      "addService post install commands missing or incorrect")
    ()
}
