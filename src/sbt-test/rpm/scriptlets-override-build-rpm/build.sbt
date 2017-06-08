import RpmConstants._
enablePlugins(JavaServerAppPackaging)

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

maintainerScripts in Rpm := Map(
  Pre -> Seq("""echo "pre-install""""),
  Post -> Seq("""echo "post-install""""),
  Pretrans -> Seq("""echo "pretrans""""),
  Posttrans -> Seq("""echo "posttrans""""),
  Preun -> Seq("""echo "pre-uninstall""""),
  Postun -> Seq("""echo "post-uninstall"""")
)

TaskKey[Unit]("check-spec-file") := {
  val spec = IO.read(target.value / "rpm" / "SPECS" / "rpm-test.spec")
  assert(spec contains "%pre\necho \"pre-install\"", "Spec doesn't contain %pre scriptlet")
  assert(spec contains "%post\necho \"post-install\"", "Spec doesn't contain %post scriptlet")
  assert(spec contains "%pretrans\necho \"pretrans\"", "Spec doesn't contain %pretrans scriptlet")
  assert(spec contains "%posttrans\necho \"posttrans\"", "Spec doesn't contain %posttrans scriptlet")
  assert(spec contains "%preun\necho \"pre-uninstall\"", "Spec doesn't contain %preun scriptlet")
  assert(spec contains "%postun\necho \"post-uninstall\"", "Spec doesn't contain %postun scriptlet")
  // Checking for the stuff that should be overriden
  assert(!(spec contains "groupadd --system rpm-test"), "Groupadd should be overridden \n" + spec)
  assert(
    !(spec contains "useradd --gid rpm-test --no-create-home --system -c 'Test rpm package' rpm-test"),
    "Useradd should be overridden \n" + spec
  )
  assert(!(spec contains "groupdel rpm-test"), "Groupdel should be overridden \n" + spec)
  assert(!(spec contains "userdel rpm-test"), "Userdel should be overridden in \n" + spec)
  streams.value.log.success("Successfully tested rpm test file")
  ()
}
