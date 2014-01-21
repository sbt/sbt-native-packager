import NativePackagerKeys._
import com.typesafe.sbt.packager.archetypes.ServerLoader

packageArchetype.java_server

serverLoading in Debian := ServerLoader.Upstart

daemonUser in Debian := "root"

mainClass in Compile := Some("empty")

name := "debian-test"

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

