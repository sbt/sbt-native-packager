enablePlugins(DebianPlugin)

name := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

Debian / debianPackageDependencies ++= Seq("java2-runtime", "bash (>= 2.05a-11)")

Debian / debianPackageRecommends += "git"

(Debian / debianChangelog) := Some(file("debian/changelog"))
