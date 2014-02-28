import NativePackagerKeys._
import com.typesafe.sbt.packager.archetypes.ServerLoader

packageArchetype.java_server

serverLoading in Debian := ServerLoader.Upstart

daemonUser in Linux := "daemonuser"

daemonGroup in Linux := "daemongroup"

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
  assert(postinst contains "addgroup --system daemongroup", "postinst misses addgroup for daemongroup: " + postinst)
  assert(postinst contains "useradd --system --no-create-home --gid daemongroup --shell /bin/false daemonuser", "postinst misses useradd for daemonuser: " + postinst)
  assert(postinst contains "chown daemonuser:daemongroup /var/log/debian-test", "postinst misses chown daemonuser /var/log/debian-test: " + postinst)
  assert(postinst contains "chown daemonuser:daemongroup /usr/share/debian-test/bin/debian-test", "postinst misses chown daemonuser /usr/share/debian-test/bin/debian-test: " + postinst)
  assert(!(postinst contains "addgroup --system daemonuser"), "postinst has addgroup for daemonuser: " + postinst)
  assert(!(postinst contains "useradd --system --no-create-home --gid daemonuser --shell /bin/false daemonuser"), "postinst has useradd for daemongroup: " + postinst)
  assert(postrm contains "deluser --quiet --system daemonuser > /dev/null || true", "postrm misses purging daemonuser user: " + postrm)
  assert(postrm contains "delgroup --quiet --system daemongroup > /dev/null || true", "postrm misses purging daemongroup group: " + postrm)
  out.log.success("Successfully tested upstart control files")
  ()
}

