enablePlugins(JavaServerAppPackaging)

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
    assert(scriptlets contains "addGroup rpm-test", "addGroup not present in \n" + scriptlets)
    assert(scriptlets contains "addUser rpm-test", "Incorrect useradd command in \n" + scriptlets)
    assert(scriptlets contains "deleteGroup rpm-test", "deleteGroup not present in \n" + scriptlets)
    assert(scriptlets contains "deleteUser rpm-test", "deleteUser rpm not present in \n" + scriptlets)
    // TODO check symlinks
    ()
}

TaskKey[Unit]("check-spec-file") <<= (target, streams) map { (target, out) =>
    val spec = IO.read(target / "rpm" / "SPECS" / "rpm-test.spec")
    assert(spec contains "addGroup rpm-test", "addGroup not present in \n" + spec)
    assert(spec contains "addUser rpm-test", "Incorrect useradd command in \n" + spec)
    assert(spec contains "deleteGroup rpm-test", "deleteGroup not present in \n" + spec)
    assert(spec contains "deleteUser rpm-test", "deleteUser rpm not present in \n" + spec)
    assert(spec contains
      """
        |[ -e /etc/sysconfig/rpm-test ] && sed -i 's/PACKAGE_PREFIX\=.*//g' /etc/sysconfig/rpm-test
        |[ -n "$RPM_INSTALL_PREFIX" ] && echo "PACKAGE_PREFIX=${RPM_INSTALL_PREFIX}" >> /etc/sysconfig/rpm-test
        |""".stripMargin, "Persisting $RPM_INSTALL_PREFIX not present in \n" + spec)
    ()
}
