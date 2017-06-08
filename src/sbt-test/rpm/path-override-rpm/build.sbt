enablePlugins(JavaServerAppPackaging, SystemVPlugin)

name := "rpm-test"
version := "0.1.0"
maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test rpm package"

executableScriptName := "rpm-exec"

packageDescription := """A fun package description of our software,
  with multiple lines."""

rpmRelease := "1"
rpmVendor := "typesafe"
rpmUrl := Some("http://github.com/sbt/sbt-native-packager")
rpmLicense := Some("BSD")
rpmGroup := Some("test-group")

defaultLinuxInstallLocation := "/opt/test"
defaultLinuxLogsLocation := "/opt/test/log"

TaskKey[Unit]("unzip") := {
  val rpmPath = Seq((packageBin in Rpm).value.getAbsolutePath)
  Process("rpm2cpio", rpmPath) #| Process("cpio -i --make-directories") ! streams.value.log
  ()
}

TaskKey[Unit]("check-init-file") := {
  val initd = IO.read(baseDirectory.value / "etc" / "init.d" / "rpm-test")
  assert(initd contains "/opt/test/rpm-test", "defaultLinuxInstallLocation not overriden in init.d\n" + initd)
  assert(
    initd contains "/opt/test/log/rpm-test/$logfile",
    "defaultLinuxLogsLocation not overriden in init.d\n" + initd
  )
  streams.value.log.success("Successfully tested rpm-test file")
  ()
}
