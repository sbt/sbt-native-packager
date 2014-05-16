import NativePackagerKeys._
import com.typesafe.sbt.packager.archetypes.ServerLoader

packageArchetype.java_server

serverLoading in Rpm := ServerLoader.Systemd

name := "rpm-test"

version := "0.1.0"

maintainer := "Alexey Kardapoltsev <alexey.kardapoltsev@frumatic.com>"

packageSummary := "Test rpm package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

rpmRelease := "1"

rpmVendor := "typesafe"

rpmUrl := Some("http://github.com/sbt/sbt-native-packager")

rpmLicense := Some("BSD")

requiredStartFacilities in Rpm := "serviceA.service"

TaskKey[Unit]("unzip") <<= (packageBin in Rpm, streams) map { (rpmFile, streams) =>
  val rpmPath = Seq(rpmFile.getAbsolutePath)
  Process("rpm2cpio" , rpmPath) #| Process("cpio -i --make-directories") ! streams.log
  ()
}

TaskKey[Unit]("checkStartupScript") <<= (target, streams) map { (target, out) =>
  val script = IO.read(file("usr/lib/systemd/system/rpm-test.service"))
  val runScript = file("usr/share/rpm-test/bin/rpm-test")
  assert(script.contains("Requires=serviceA.service"), "script doesn't contain Default-Start header\n" + script)
  out.log.success("Successfully tested systemd start up script")
  ()
}
