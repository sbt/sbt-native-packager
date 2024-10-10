enablePlugins(JavaServerAppPackaging, UpstartPlugin)

// TODO change this after #437 is fixed
(Linux / daemonUser) := "root"
(Linux / daemonGroup) := "app-group"

(Compile / mainClass) := Some("empty")

name := "debian-test"
version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"
packageSummary := "Test debian package"
packageDescription := """A fun package description of our software,
  with multiple lines."""

TaskKey[Unit]("checkControlFiles") := {
  val debian = target.value / "debian-test-0.1.0" / "DEBIAN"
  val postinst = IO.read(debian / "postinst")
  val prerm = IO.read(debian / "prerm")
  assert(postinst contains "initctl reload-configuration", "postinst misses initctl: " + postinst)
  assert(postinst contains """startService debian-test""", "postinst misses service start: " + postinst)
  assert(prerm contains """stopService debian-test""", "prerm misses stop: " + prerm)
  streams.value.log.success("Successfully tested upstart control files")
  ()
}

InputKey[Unit]("checkSoftlink") := {
  import complete.DefaultParsers._
  val args = spaceDelimited("<args>").parsed
  assert(args.size >= 2, "Usage: check-softlink link to target")
  val link = args(0)
  val target = args(args.size - 1)
  val absolutePath = sys.process.Process("readlink -m " + link).!!.trim
  assert(link != absolutePath, "Expected symbolic link '" + link + "' does not exist")
  assert(
    target == absolutePath,
    "Expected symbolic link '" + link + "' to point to '" + target + "', but instead it's '" + absolutePath + "'"
  )
}

TaskKey[Unit]("checkStartupScript") := {
  val script = IO.read(target.value / "debian-test-0.1.0" / "etc" / "init" / "debian-test.conf")
  assert(script.contains("start on runlevel [2345]"), "script doesn't contain start on runlevel header\n" + script)
  assert(script.contains("stop on runlevel [016]"), "script doesn't contain stop on runlevel header\n" + script)
  assert(!script.contains("start on started"), "script contains start on started header\n" + script)
  assert(!script.contains("stop on stopping"), "script contains stop on stopping header\n" + script)
  // should contain
  assert(
    script contains "[ -d /var/run/debian-test ] || install -m 755 -o root -g app-group -d /var/run/debian-test",
    "Script is missing /var/run dir install\n" + script
  )
  ()
}

TaskKey[Unit]("checkAutostart") := {
  val script = IO.read(target.value / "debian-test-0.1.0" / "DEBIAN" / "postinst")
  assert(
    script.contains("""addService debian-test || echo "debian-test could not be registered"
      |startService debian-test || echo "debian-test could not be started"
      |""".stripMargin),
    "addService, startService post install commands missing or incorrect"
  )
  ()
}

TaskKey[Unit]("checkNoAutostart") := {
  val script = IO.read(target.value / "debian-test-0.1.0" / "DEBIAN" / "postinst")
  assert(
    script.contains("""addService debian-test || echo "debian-test could not be registered"
      |""".stripMargin),
    "addService post install commands missing or incorrect"
  )
  ()
}
