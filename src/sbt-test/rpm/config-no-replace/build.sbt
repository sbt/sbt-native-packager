import com.typesafe.sbt.packager.Compat._

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

packageArchitecture in Rpm := "x86_64"

linuxPackageMappings := configWithNoReplace(linuxPackageMappings.value)

TaskKey[Unit]("unzip") := {
  val rpmPath = Seq((packageBin in Rpm).value.getAbsolutePath)
  sys.process.Process("rpm2cpio", rpmPath) #| sys.process.Process("cpio -i --make-directories") ! streams.value.log
}

TaskKey[Unit]("checkSpecFile") := {
  val spec = IO.read(target.value / "rpm" / "SPECS" / "rpm-test.spec")

  assert(
    spec contains
      "%files\n%dir %attr(0755,root,root) /usr/share/rpm-test/conf",
    "Contains configuration directory."
  )

  assert(
    spec contains
      "%config(noreplace) %attr(0644,root,root) /usr/share/rpm-test/conf/test",
    "Sets custom config to 'noreplace'"
  )

  streams.value.log.success("Successfully tested rpm test file")
  ()
}
