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

TaskKey[Unit]("unzip") <<= (baseDirectory, packageBin in Rpm, streams) map { (baseDir, rpmFile, streams) =>
  val rpmPath = Seq(rpmFile.getAbsolutePath)
  Process("rpm2cpio" , rpmPath) #| Process("cpio -i --make-directories") ! streams.log
  ()
}

TaskKey[Unit]("check-init-file") <<= (baseDirectory, streams) map { (target, out) =>
  val initd = IO.read(target / "etc" / "init.d" / "rpm-test")
  assert(initd contains "/opt/test/rpm-test", "defaultLinuxInstallLocation not overriden in init.d\n" + initd)
  assert(initd contains "/opt/test/log/rpm-test/$logfile", "defaultLinuxLogsLocation not overriden in init.d\n" + initd)
  out.log.success("Successfully tested rpm-test file")
  ()
}
