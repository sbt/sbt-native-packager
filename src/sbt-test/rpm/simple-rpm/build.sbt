enablePlugins(RpmPlugin)

name := "rpm-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test rpm package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

rpmRelease := "1"

rpmVendor := "typesafe"

rpmUrl := Some("http://github.com/sbt/sbt-native-packager")

rpmLicense := Some("BSD")

TaskKey[Unit]("unzip") <<= (packageBin in Rpm, streams) map { (rpmFile, streams) =>
  val rpmPath = Seq(rpmFile.getAbsolutePath)
  Process("rpm2cpio" , rpmPath) #| Process("cpio -i --make-directories") ! streams.log
}

TaskKey[Unit]("checkSpecFile") <<= (target, streams) map { (target, out) =>
  val spec = IO.read(target / "rpm" / "SPECS" / "rpm-test.spec")
  println(spec)

  assert(spec contains "Name: rpm-test", "Contains project name")
  assert(spec contains "Version: 0.1.0", "Contains project version")
  assert(spec contains "Release: 1", "Contains project release")
  assert(spec contains "Summary: Test rpm package", "Contains project summary")
  assert(spec contains "License: BSD", "Contains project license")
  assert(spec contains "Vendor: typesafe", "Contains project vendor")
  assert(spec contains "URL: http://github.com/sbt/sbt-native-packager", "Contains project url")
  assert(spec contains "BuildArch: noarch", "Contains noarch for BuildArch")

  assert(spec contains
    "%description\nA fun package description of our software,\n  with multiple lines.",
    "Contains project description"
  )

  out.log.success("Successfully tested rpm test file")
  ()
}
