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

TaskKey[Unit]("checkLoaderScript") := {
  val path = target.value / "rpm" / "RPMS" / "noarch" / "rpm-test-0.1.0-1.noarch.rpm"
  val scripts = s"rpm -qp --scripts ${path.absolutePath}".!!

  assert(scripts.contains("# right systemd template"), s"override script wasn't picked, scripts are\n$scripts")
  assert(!scripts.contains("wrong start template"), s"scripts contained wrong template, scripts are\n$scripts")
  ()
}
