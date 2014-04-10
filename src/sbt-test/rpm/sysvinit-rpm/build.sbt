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
    assert(scriptlets contains "groupadd --system rpm-test", "groupadd not present in \n" + scriptlets)
    assert(scriptlets contains "useradd --gid rpm-test --no-create-home --system -c 'Test rpm package' rpm-test", "Incorrect useradd command in \n" + scriptlets)
    assert(scriptlets contains "groupdel rpm-test", "groupdel not present in \n" + scriptlets)
    assert(scriptlets contains "userdel rpm-test", "userdel rpm not present in \n" + scriptlets)
    // TODO check symlinks
    ()
}

TaskKey[Unit]("check-spec-file") <<= (target, streams) map { (target, out) =>
    val spec = IO.read(target / "rpm" / "SPECS" / "rpm-test.spec")
    assert(spec contains "groupadd --system rpm-test", "groupadd not present in \n" + spec)
    assert(spec contains "useradd --gid rpm-test --no-create-home --system -c 'Test rpm package' rpm-test", "Incorrect useradd command in \n" + spec)
    assert(spec contains "groupdel rpm-test", "groupdel not present in \n" + spec)
    assert(spec contains "userdel rpm-test", "userdel rpm not present in \n" + spec)
    ()
}
