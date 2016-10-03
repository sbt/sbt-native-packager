enablePlugins(JDebPackaging)

name := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

debianPackageConflicts in Debian += "debian-other-test-package"

debianPackageDependencies in Debian ++= Seq("java2-runtime",
                                            "bash (>= 2.05a-11)")

debianPackageProvides in Debian += "debian-test-package"

debianPackageRecommends in Debian += "git"
