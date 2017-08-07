import com.typesafe.sbt.packager.linux.LinuxPackageMapping

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
packageArchitecture in Rpm := "i386"

rpmSetarch := Some("i386")

linuxPackageMappings := {
  val helloMapping = LinuxPackageMapping(
      Seq(((resourceDirectory in Compile).value / "hello-32bit", "/usr/share/rpm-package/libexec/hello-32bit"))
    ) withPerms "0755"
  linuxPackageMappings.value :+ helloMapping
}

TaskKey[Unit]("checkSpecFile") := {
  val spec = IO.read(target.value / "rpm" / "SPECS" / "rpm-package.spec")
  streams.value.log.success(spec)
  assert(
    spec contains "%attr(0644,root,root) /usr/share/rpm-package/lib/rpm-test.rpm-test-0.1.0.jar",
    "Wrong installation path\n" + spec
  )
  assert(
    spec contains "%attr(0755,root,root) /usr/share/rpm-package/libexec/hello-32bit",
    "Wrong 32-bit exe installation path\n" + spec
  )
  streams.value.log.success("Successfully tested rpm-package file")
  ()
}
