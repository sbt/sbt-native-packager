import NativePackagerKeys._

packageArchetype.java_server

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

mainClass in (Compile, run) := Some("com.example.MainApp")

TaskKey[Unit]("unzipAndCheck") <<= (packageBin in Rpm, streams) map { (rpmFile, streams) =>
    val rpmPath = Seq(rpmFile.getAbsolutePath)
    Process("rpm2cpio" , rpmPath) #| Process("cpio -i --make-directories") !  streams.log
    // TODO check symlinks
    ()
}

