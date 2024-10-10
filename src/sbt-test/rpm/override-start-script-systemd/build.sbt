import com.typesafe.sbt.packager.Compat._

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

TaskKey[Unit]("unzip") := {
  val rpmPath = Seq(((Rpm / packageBin)).value.getAbsolutePath)
  sys.process.Process("rpm2cpio", rpmPath) #| sys.process.Process("cpio -i --make-directories") ! streams.value.log
  ()
}

TaskKey[Unit]("checkStartupScript") := {
  val script = IO.read(file("usr/lib/systemd/system/rpm-test.service"))
  assert(script.startsWith("# right systemd template"), s"override script wasn't picked, script is\n$script")
  ()
}
