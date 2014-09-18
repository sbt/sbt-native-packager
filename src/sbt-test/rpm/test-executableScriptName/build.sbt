import NativePackagerKeys._

packageArchetype.java_server

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

TaskKey[Unit]("check-spec-file") <<= (target, streams) map { (target, out) =>
  val spec = IO.read(target / "rpm" / "SPECS" / "rpm-test.spec")
  out.log.success(spec)
  assert(spec contains "%attr(0644,root,root) /usr/share/rpm-test/lib/rpm-test.rpm-test-0.1.0.jar", "Wrong installation path\n" + spec)
  assert(spec contains "%config %attr(0755,root,root) /etc/init.d/rpm-test", "Wrong /etc/init.d/\n" + spec)
  assert(spec contains "%config %attr(644,root,root) /etc/default/rpm-test", "Wrong etc default file\n" + spec)
  assert(spec contains "%dir %attr(755,rpm-test,rpm-test) /var/log/rpm-test", "Wrong logging dir path\n" + spec)
  assert(spec contains "%dir %attr(755,rpm-test,rpm-test) /var/run/rpm-test", "Wrong /var/run dir path\n" + spec)
  out.log.success("Successfully tested rpm-test file")
  ()
}

TaskKey[Unit]("unzip") <<= (packageBin in Rpm, streams) map { (rpmFile, streams) =>
  val rpmPath = Seq(rpmFile.getAbsolutePath)
  Process("rpm2cpio" , rpmPath) #| Process("cpio -i --make-directories") ! streams.log
  ()
}

TaskKey[Unit]("checkStartupScript") <<= (target, streams) map { (target, out) =>
  val script = IO.read(file("etc/init.d/rpm-test"))
  assert(script contains "rpm-exec", "SystemV script didn't contain correct executable filename 'rpm-exec' \n" + script)
  out.log.success("Successfully tested startup script start up script")
  ()
}