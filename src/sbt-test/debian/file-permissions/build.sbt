enablePlugins(DebianPlugin)

name := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

linuxPackageMappings += packageMapping( 
  ((resourceDirectory in Compile).value / "sudoers.d", "/etc/sudoers.d")
) withPerms("0440") asDocs()