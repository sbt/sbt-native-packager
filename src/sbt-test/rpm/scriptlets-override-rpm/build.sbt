import NativePackagerKeys._

packageArchetype.java_server

name := "rpm-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test rpm package"

packageDescription := "Description"

rpmRelease := "1"

rpmVendor := "typesafe"

rpmUrl := Some("http://github.com/sbt/sbt-native-packager")

rpmLicense := Some("BSD")

mainClass in (Compile, run) := Some("com.example.MainApp")

TaskKey[Unit]("unzipAndCheck") <<= (packageBin in Rpm, streams) map { (rpmFile, streams) =>
    val rpmPath = Seq(rpmFile.getAbsolutePath)
    Process("rpm2cpio" , rpmPath) #| Process("cpio -i --make-directories") ! streams.log
    val scriptlets = Process("rpm -qp --scripts " + rpmFile.getAbsolutePath) !! streams.log
    assert(scriptlets contains "echo postinst", "'echo 'postinst' not present in \n" + scriptlets)
    assert(scriptlets contains "echo preinst", "'echo 'preinst' not present in \n" + scriptlets)
    assert(scriptlets contains "echo postun", "'echo 'postun' not present in \n" + scriptlets)
    assert(scriptlets contains "echo preun", "'echo 'preun' not present in \n" + scriptlets)
    ()
}

