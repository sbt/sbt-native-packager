import com.typesafe.sbt.packager.linux.{LinuxPackageMapping, LinuxSymlink}

enablePlugins(JavaAppPackaging)

name := "rpm-test"
version := "0.1.0"
maintainer := "David Pennell <dpennell@good-cloud.com>"

packageSummary := "Test rpm package"
packageName in Linux := "rpm-package"
packageDescription := """A fun package description of our software,
  with multiple lines."""

rpmRelease := "1"
rpmVendor := "typesafe"
rpmUrl := Some("http://github.com/sbt/sbt-native-packager")
rpmLicense := Some("BSD")

linuxPackageSymlinks := {
  val helloSymlink = LinuxSymlink(((file(defaultLinuxInstallLocation.value) / (packageName in Linux).value / "lib") / "hello.link").toString, "/fake/hello.txt")
  Seq(helloSymlink)
}

TaskKey[Unit]("check-spec-file") <<= (target, streams) map { (target, out) =>
  val spec = IO.read(target / "rpm" / "SPECS" / "rpm-package.spec")
  out.log.success(spec)
  assert(spec contains "%attr(0644,root,root) /usr/share/rpm-package/lib/rpm-test.rpm-test-0.1.0.jar", "Wrong installation path\n" + spec)
  assert(spec contains "/usr/share/rpm-package/lib/hello.link", "Missing or incorrect symbolic link\n" + spec)
  out.log.success("Successfully tested rpm-package file")
  ()
}
