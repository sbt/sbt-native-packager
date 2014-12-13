import com.typesafe.sbt.packager.archetypes.ServerLoader

enablePlugins(JavaServerAppPackaging)

serverLoading in Debian := ServerLoader.Upstart

// TODO change this after #437 is fixed
daemonUser in Linux := "root"

daemonGroup in Linux := "app-group"

mainClass in Compile := Some("empty")

name := "debian-test"

name in Debian := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

TaskKey[Unit]("check-control-files") <<= (target, streams) map { (target, out) =>
  val debian = target / "debian-test-0.1.0" / "DEBIAN"
  val postinst = IO.read(debian / "postinst")
  val prerm = IO.read(debian / "prerm")
  assert(postinst contains "initctl reload-configuration", "postinst misses initctl: " + postinst)
  assert(postinst contains """startService debian-test""", "postinst misses service start: " + postinst)
  assert(prerm contains """stopService debian-test""", "prerm misses stop: " + prerm)
  out.log.success("Successfully tested upstart control files")
  ()
}

InputKey[Unit]("check-softlink") <<= inputTask { (argTask: TaskKey[Seq[String]]) =>
  (argTask) map { (args: Seq[String]) =>
    assert(args.size >= 2, "Usage: check-softlink link to target")
    val link = args(0)
    val target = args(args.size - 1)
    val absolutePath = ("readlink -m " + link).!!.trim
    assert(link != absolutePath, "Expected symbolic link '" + link + "' does not exist")
    assert(target == absolutePath, "Expected symbolic link '" + link + "' to point to '" + target + "', but instead it's '" + absolutePath + "'")
  }
}

TaskKey[Unit]("check-startup-script") <<= (target, streams) map { (target, out) =>
  val script = IO.read(target / "debian-test-0.1.0" / "etc" / "init" / "debian-test.conf")
  assert(script.contains("start on runlevel [2345]"), "script doesn't contain start on runlevel header\n" + script)
  assert(script.contains("stop on runlevel [016]"), "script doesn't contain stop on runlevel header\n" + script)
  assert(!script.contains("start on started"), "script contains start on started header\n" + script)
  assert(!script.contains("stop on stopping"), "script contains stop on stopping header\n" + script)
  // should contain
  assert(script contains "[ -d /var/run/debian-test ] || install -m 755 -o root -g app-group -d /var/run/debian-test", "Script is missing /var/run dir install\n" + script)
  ()
}
