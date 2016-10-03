enablePlugins(JavaServerAppPackaging)

daemonUser in Linux := "daemonuser"
daemonUserUid in Linux := Some("20000")
daemonGroup in Linux := "daemongroup"

mainClass in Compile := Some("empty")

name := "debian-test"
version := "0.1.0"
maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"
packageDescription := """A fun package description of our software,
  with multiple lines."""

TaskKey[Unit]("check-control-files") <<= (target, streams) map {
  (target, out) =>
    val debian = target / "debian-test-0.1.0" / "DEBIAN"
    val postinst = IO.read(debian / "postinst")
    val postrm = IO.read(debian / "postrm")
    assert(postinst contains """addGroup daemongroup """"",
           "postinst misses addgroup for daemongroup: " + postinst)
    assert(
      postinst contains """addUser daemonuser "20000" daemongroup "debian-test daemon-user" "/bin/false"""",
      "postinst misses useradd for daemonuser: " + postinst)
    assert(
      postinst contains "chown daemonuser:daemongroup /var/log/debian-test",
      "postinst misses chown daemonuser /var/log/debian-test: " + postinst)
    assert(!(postinst contains "addgroup --system daemonuser"),
           "postinst has addgroup for daemonuser: " + postinst)
    assert(
      !(postinst contains "useradd --system --no-create-home --gid daemonuser --shell /bin/false daemonuser"),
      "postinst has useradd for daemongroup: " + postinst)
    assert(postrm contains "deleteUser daemonuser",
           "postrm misses purging daemonuser user: " + postrm)
    assert(postrm contains "deleteGroup daemongroup",
           "postrm misses purging daemongroup group: " + postrm)
    assert(!(postinst contains "chown debian-test:daemongroup"),
           "postinst contains wrong user: \n" + postinst)
    assert(!(postinst contains "chown daemonuser:debian-test"),
           "postinst contains wrong group: \n" + postinst)
    assert(!(postinst contains "chown debian-test:debian-test"),
           "postinst contains wrong user and group: \n" + postinst)
    assert(
      !(postinst contains "chown daemonuser:daemongroup /usr/share/debian-test"),
      "postinst contains chown /usr/share/app_name:  \n" + postinst)
    out.log.success("Successfully tested upstart control files")
    ()
}
