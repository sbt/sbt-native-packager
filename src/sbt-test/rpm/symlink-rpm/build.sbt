import com.typesafe.sbt.packager.linux.{LinuxPackageMapping, LinuxSymlink}

enablePlugins(JavaAppPackaging)

name := "rpm-test"
version := "0.1.0"
maintainer := "David Pennell <dpennell@good-cloud.com>"
packageSummary := "Test rpm package"
(Linux / packageName) := "rpm-package"
packageDescription :=
  """A fun package description of our software,
  with multiple lines."""

rpmRelease := "1"
rpmVendor := "typesafe"
rpmUrl := Some("http://github.com/sbt/sbt-native-packager")
rpmLicense := Some("BSD")

linuxPackageSymlinks := {
  val helloSymlink = LinuxSymlink(
    ((file(defaultLinuxInstallLocation.value) / (Linux / packageName).value / "lib") / "hello.link").toString,
    "/fake/hello.tx"
  )
  Seq(helloSymlink)
}

TaskKey[Unit]("check-spec-file") := {
  val spec = IO.read(target.value / "rpm" / "SPECS" / "rpm-package.spec")
  streams.value.log.success(spec)
  assert(
    spec contains "%attr(0644,root,root) /usr/share/rpm-package/lib/rpm-test.rpm-test-0.1.0.jar",
    "Wrong installation path"
  )
  assert(spec contains "/usr/share/rpm-package/lib/hello.link", "Missing or incorrect symbolic link")
  streams.value.log.success("Successfully tested rpm-package file")
  ()
}
