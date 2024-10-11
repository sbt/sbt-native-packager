enablePlugins(JavaServerAppPackaging)

Linux / daemonUser := "daemonuser"
Linux / daemonGroup := "daemongroup"
Linux / daemonShell := "/bin/bash"

Compile / mainClass := Some("empty")

name := "debian-test"
version := "0.1.0"
maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"
packageDescription := """A fun package description of our software,
  with multiple lines."""

TaskKey[Unit]("checkControlFiles") := {
  val debian = target.value / "debian-test-0.1.0" / "DEBIAN"
  val postinst = IO.read(debian / "postinst")
  val postrm = IO.read(debian / "postrm")
  assert(
    postinst contains """addUser daemonuser "" daemongroup "debian-test daemon-user" "/bin/bash"""",
    "postinst misses useradd for daemonuser: " + postinst
  )
  ()
}
