enablePlugins(JavaServerAppPackaging, SystemdPlugin)

name := "rpm-test"

version := "0.1.0"

maintainer := "Mitch Seymour <mitchseymour@gmail.com>"

packageSummary := "Test rpm package"

executableScriptName := "rpm-exec"

packageDescription := """A fun package description of our software,
  with multiple lines."""

rpmRelease := "1"

rpmVendor := "typesafe"

rpmUrl := Some("http://github.com/sbt/sbt-native-packager")

rpmLicense := Some("BSD")

rpmGroup := Some("test-group")

TaskKey[Unit]("unzip") <<= (packageBin in Rpm, streams) map { (rpmFile, streams) =>
  val rpmPath = Seq(rpmFile.getAbsolutePath)
  Process("rpm2cpio", rpmPath) #| Process("cpio -i --make-directories") ! streams.log
  ()
}

TaskKey[Unit]("checkStartupScript") <<= (target, streams) map { (target, out) =>
  val script = IO.read(file("usr/lib/systemd/system/rpm-test.service"))
  assert(script.startsWith("# right systemd template"), s"override script wasn't picked, script is\n$script")
  ()
}