import NativePackagerKeys._
import com.typesafe.sbt.packager.archetypes.ServerLoader

packageArchetype.java_server

serverLoading in Debian := ServerLoader.Upstart

daemonUser in Debian := "root"

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
  assert(postinst contains """service debian-test start || echo "debian-test could not be started. Try manually with service debian-test start"""", "postinst misses service start: " + postinst)
  assert(prerm contains """service debian-test stop || echo "debian-test wasn't even running!"""", "prerm misses stop: " + prerm)
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
  assert(script.contains("start on started [networking]"), "script doesn't contain start on [networking] header\n" + script)
  assert(script.contains("stop on runlevel [016]"), "script doesn't contain stop on runlevel header\n" + script)
  assert(script.contains("stop on stopping [networking]"), "script doesn't contain stop on [networking] header\n" + script)
  out.log.success("Successfully tested systemV start up script")
  ()
}
