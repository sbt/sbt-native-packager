import NativePackagerKeys._
import com.typesafe.sbt.packager.archetypes.ServerLoader

packageArchetype.java_server

serverLoading in Debian := ServerLoader.Upstart

appUser in Linux := "daemonUser"

appGroup in Linux := "daemonGroup"

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
  val postrm = IO.read(debian / "postrm")
  assert(postinst contains "addgroup --system daemonGroup", "postinst misses addgroup for daemonGroup: " + postinst)
  assert(postinst contains "useradd --system --no-create-home --gid daemonGroup --shell /bin/false daemonUser", "postinst misses useradd for daemonUser: " + postinst)
  assert(postinst contains "chown daemonUser:daemonGroup /var/log/debian-test", "postinst misses chown daemonUser /var/log/debian-test: " + postinst)
  assert(postinst contains "chown daemonUser:daemonGroup /usr/share/debian-test/bin/debian-test", "postinst misses chown daemonUser /usr/share/debian-test/bin/debian-test: " + postinst)
  assert(postrm contains "deluser --quiet --system daemonUser > /dev/null || true", "postrm misses purging daemonUser user: " + postrm)
  assert(postrm contains "delgroup --quiet --system daemonGroup > /dev/null || true", "postrm misses purging daemonGroup group: " + postrm)
  out.log.success("Successfully tested upstart control files")
  ()
}

