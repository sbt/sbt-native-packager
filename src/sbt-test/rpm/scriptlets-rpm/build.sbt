import com.typesafe.sbt.packager.Compat._

import RpmConstants._

enablePlugins(RpmPlugin)

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
  assert(
    spec contains
      """
      |%post
      |echo "post-install"
      |
      |relocateLink() {
      |  if [ -n "$4" ] ;
      |  then
      |    RELOCATED_INSTALL_DIR="$4/$3"
      |    echo "${1/$2/$RELOCATED_INSTALL_DIR}"
      |  else
      |    echo "$1"
      |  fi
      |}
      |rm -rf $(relocateLink /etc/rpm-test /usr/share/rpm-test rpm-test $RPM_INSTALL_PREFIX) && ln -s $(relocateLink /usr/share/rpm-test/conf /usr/share/rpm-test rpm-test $RPM_INSTALL_PREFIX) $(relocateLink /etc/rpm-test /usr/share/rpm-test rpm-test $RPM_INSTALL_PREFIX)
      |""".stripMargin,
    "%post scriptlet does not contain relocateLink"
  )

  assert(spec contains "%pretrans\necho \"pretrans\"", "Spec doesn't contain %pretrans scriptlet")
  assert(spec contains "%posttrans\necho \"posttrans\"", "Spec doesn't contain %posttrans scriptlet")
  assert(spec contains "%preun\necho \"pre-uninstall\"", "Spec doesn't contain %preun scriptlet")
  assert(spec contains "%postun\necho \"post-uninstall\"", "Spec doesn't contain %postun scriptlet")
  assert(
    spec contains
      """
      |%postun
      |echo "post-uninstall"
      |
      |relocateLink() {
      |  if [ -n "$4" ] ;
      |  then
      |    RELOCATED_INSTALL_DIR="$4/$3"
      |    echo "${1/$2/$RELOCATED_INSTALL_DIR}"
      |  else
      |    echo "$1"
      |  fi
      |}
      |if [ $1 -eq 0 ] ;
      |then
      |  [ -e /etc/sysconfig/rpm-test ] && . /etc/sysconfig/rpm-test
      |  rm -rf $(relocateLink /etc/rpm-test /usr/share/rpm-test rpm-test $PACKAGE_PREFIX)
      |fi
      |""".stripMargin,
    "%postun scriptlet does not contain relocate link"
  )
  streams.value.log.success("Successfully tested rpm test file")
  ()
}

TaskKey[Unit]("check-rpm-version") := {
  val fullRpmVersion = Process("rpm", Seq("--version")) !!
  val firstDigit = fullRpmVersion indexWhere Character.isDigit
  val rpmVersion = fullRpmVersion substring firstDigit
  streams.value.log.info("Found rpmVersion: " + rpmVersion)
  val (major, minor, patch) = rpmVersion.trim.split("\\.").map(_.toInt) match {
    case Array(major)                    => (major, 0, 0)
    case Array(major, minor)             => (major, minor, 0)
    case Array(major, minor, patch, _ *) => (major, minor, patch)
  }
  assert(major >= 4, "RPM version must be greater than than 4.x.x. Is " + fullRpmVersion)
  ()
}
