enablePlugins(DebianPlugin)

name := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

Debian / debianPackageConflicts += "debian-other-test-package"

Debian / debianPackageDependencies ++= Seq("java2-runtime", "bash (>= 2.05a-11)")

Debian / debianPackageProvides += "debian-test-package"

Debian / debianPackageRecommends += "git"
