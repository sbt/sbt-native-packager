import com.typesafe.sbt.packager.linux._

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

linuxPackageMappings in Rpm := {
  val mapping1 = ((baseDirectory.value / "test"), "tmp/test")
  val mapping2 = ((baseDirectory.value / "build.sbt"), "/tmp/build.sbt")
  Seq(LinuxPackageMapping(Seq(mapping1, mapping2)))
}

linuxPackageSymlinks in Rpm := Seq(
  LinuxSymlink("/etc/link1", "destination1"),
  LinuxSymlink("link2", "destination2")
)

defaultLinuxInstallLocation in Rpm := "/opt/foo"

TaskKey[Unit]("unzip") <<= (packageBin in Rpm, streams) map { (rpmFile, streams) =>
  val rpmPath = Seq(rpmFile.getAbsolutePath)
  Process("rpm2cpio" , rpmPath) #| Process("cpio -i --make-directories") ! streams.log
}

TaskKey[Unit]("checkSpecFile") <<= (target, streams) map { (target, out) =>
  val spec = IO.read(target / "rpm" / "SPECS" / "rpm-test.spec")
  assert(spec contains "Name: rpm-test", "Contains project name")
  assert(spec contains "Version: 0.1.0", "Contains project version")
  assert(spec contains "Release: 1", "Contains project release")
  assert(spec contains "Summary: Test rpm package", "Contains project summary")
  assert(spec contains "License: BSD", "Contains project license")
  assert(spec contains "Vendor: typesafe", "Contains project vendor")
  assert(spec contains "URL: http://github.com/sbt/sbt-native-packager", "Contains project url")
  assert(spec contains "BuildArch: x86_64", "Contains project package architecture")

  assert(spec contains
    "%description\nA fun package description of our software,\n  with multiple lines.",
    "Contains project description"
  )

  assert(spec contains
    "%files\n%attr(755,root,root) /tmp/test\n%attr(755,root,root) /tmp/build.sbt",
    "Contains package mappings"
  )

  assert(spec contains
    "ln -s $(relocateLink destination1 /opt/foo/rpm-test rpm-test $RPM_INSTALL_PREFIX) $(relocateLink /etc/link1 /opt/foo/rpm-test rpm-test $RPM_INSTALL_PREFIX)",
    "Contains package symlink link (1)"
  )

  assert(spec contains
    "ln -s $(relocateLink destination2 /opt/foo/rpm-test rpm-test $RPM_INSTALL_PREFIX) $(relocateLink link2 /opt/foo/rpm-test rpm-test $RPM_INSTALL_PREFIX)",
    "Contains package symlink link (2)"
  )

  out.log.success("Successfully tested rpm test file")
  ()
}
