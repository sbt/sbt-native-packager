import com.typesafe.sbt.packager.PluginCompat
import xsbti.FileConverter

enablePlugins(JavaServerAppPackaging, SystemVPlugin)

name := "rpm-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test rpm package"

Linux / packageName := "rpm-package"

(Rpm / packageBin / artifactPath) := {
  implicit val converter: FileConverter = fileConverter.value
  PluginCompat.toFileRef(target.value / s"${(Rpm / packageName).value}-${(Rpm / version).value}.rpm")
}

packageDescription := """A fun package description of our software,
  with multiple lines."""

rpmRelease := "1"

rpmVendor := "typesafe"

rpmUrl := Some("http://github.com/sbt/sbt-native-packager")

rpmLicense := Some("BSD")

TaskKey[Unit]("checkSpecFile") := {
  val spec = IO.read(target.value / "rpm" / "SPECS" / "rpm-package.spec")
  streams.value.log.success(spec)
  assert(
    spec contains "%attr(0644,root,root) /usr/share/rpm-package/lib/rpm-test.rpm-test-0.1.0.jar",
    "Wrong installation path\n" + spec
  )
  assert(spec contains "%attr(0755,root,root) /etc/init.d/rpm-package", "Wrong /etc/init.d path\n" + spec)
  assert(spec contains "%config %attr(644,root,root) /etc/default/rpm-package", "Wrong /etc default file\n" + spec)
  assert(
    spec contains "%dir %attr(755,rpm-package,rpm-package) /var/log/rpm-package",
    "Wrong logging dir path\n" + spec
  )
  assert(
    spec contains "%dir %attr(755,rpm-package,rpm-package) /var/run/rpm-package",
    "Wrong /var/run dir path\n" + spec
  )
  streams.value.log.success("Successfully tested rpm-package file")
  ()
}
