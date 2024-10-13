import com.typesafe.sbt.packager.Compat._

enablePlugins(JavaServerAppPackaging, SystemVPlugin)

name := "rpm-test"
version := "0.1.0"
maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test rpm package"
packageDescription := "Description"

rpmRelease := "1"
rpmVendor := "typesafe"
rpmUrl := Some("http://github.com/sbt/sbt-native-packager")
rpmLicense := Some("BSD")
rpmGroup := Some("test-group")
rpmDaemonLogFile := "test.log"

(Compile / run / mainClass) := Some("com.example.MainApp")

TaskKey[Unit]("unzipAndCheck") := {
  val rpmPath = Seq((Rpm / packageBin).value.getAbsolutePath)
  sys.process.Process("rpm2cpio", rpmPath) #| sys.process.Process("cpio -i --make-directories") ! streams.value.log
  val scriptlets =
    sys.process.Process("rpm -qp --scripts " + (Rpm / packageBin).value.getAbsolutePath) !! streams.value.log
  assert(scriptlets contains "addGroup rpm-test", "addGroup not present in \n" + scriptlets)
  assert(scriptlets contains "addUser rpm-test", "Incorrect useradd command in \n" + scriptlets)
  assert(scriptlets contains "deleteGroup rpm-test", "deleteGroup not present in \n" + scriptlets)
  assert(scriptlets contains "deleteUser rpm-test", "deleteUser rpm not present in \n" + scriptlets)

  val startupScript = IO.read(baseDirectory.value / "etc" / "init.d" / "rpm-test")
  assert(
    startupScript contains
      """
        |INSTALL_DIR="/usr/share/rpm-test"
        |[ -n "${PACKAGE_PREFIX}" ] && INSTALL_DIR="${PACKAGE_PREFIX}/rpm-test"
        |cd $INSTALL_DIR
        |""".stripMargin,
    "Ensuring application is running on the install directory is not present in \n" + startupScript
  )
  assert(
    startupScript contains """logfile="test.log"""",
    "Setting key rpmDaemonLogFile not present in \n" + startupScript
  )

  // TODO check symlinks
  ()
}

TaskKey[Unit]("checkSpecFile") := {
  val spec = IO.read(target.value / "rpm" / "SPECS" / "rpm-test.spec")
  assert(spec contains "addGroup rpm-test", "addGroup not present in \n" + spec)
  assert(spec contains "addUser rpm-test", "Incorrect useradd command in \n" + spec)
  assert(spec contains "deleteGroup rpm-test", "deleteGroup not present in \n" + spec)
  assert(spec contains "deleteUser rpm-test", "deleteUser rpm not present in \n" + spec)
  assert(
    spec contains
      """
        |if [ -e /etc/sysconfig/rpm-test ] ;
        |then
        |  sed -i 's/PACKAGE_PREFIX\=.*//g' /etc/sysconfig/rpm-test
        |fi
        |
        |if [ -n "$RPM_INSTALL_PREFIX" ] ;
        |then
        |  echo "PACKAGE_PREFIX=${RPM_INSTALL_PREFIX}" >> /etc/sysconfig/rpm-test
        |fi
        |""".stripMargin,
    "Persisting $RPM_INSTALL_PREFIX not present in \n" + spec
  )
  assert(
    spec contains
      """
        |#
        |# Add service for management
        |# $1 = service name
        |#
        |addService() {
        |    app_name=$1
        |    if hash update-rc.d >/dev/null 2>&1; then
        |        echo "Adding $app_name to service management using update-rc.d"
        |        update-rc.d $app_name defaults
        |    elif hash chkconfig >/dev/null 2>&1; then
        |        echo "Adding $app_name to service management using chkconfig"
        |        chkconfig --add rpm-test
        |        chkconfig $app_name on
        |    else
        |        echo "WARNING: Could not add $app_name to autostart: neither update-rc nor chkconfig found!"
        |    fi
        |}
        |""".stripMargin,
    "rpm addService() scriptlet missing or incorrect"
  )
  assert(
    spec contains
      """
        |#
        |# Start the service
        |# $1 = service name
        |#
        |startService() {
        |    app_name=$1
        |    service $app_name start
        |}
        |""".stripMargin,
    "rpm startService() scriptlet is missing or incorrect"
  )
  assert(
    spec contains
      """
        |#
        |# Removing service from autostart
        |# $1 = service name
        |#
        |stopService() {
        |    app_name=$1
        |    if hash update-rc.d >/dev/null 2>&1; then
        |        echo "Removing $app_name from autostart using update-rc.d"
        |        update-rc.d -f $app_name remove
        |        service $app_name stop
        |    elif hash chkconfig >/dev/null 2>&1; then
        |        echo "Removing $app_name from autostart using chkconfig"
        |        chkconfig $app_name off
        |        chkconfig --del $app_name
        |        service $app_name stop
        |    else
        |        echo "WARNING: Could not remove $app_name from autostart: neither update-rc nor chkconfig found!"
        |    fi
        |}
        |""".stripMargin,
    "rpm stopService() scriptlet is missing or incorrect"
  )
  assert(
    spec contains
      """
        |#
        |# Restarting the service after package upgrade
        |# $1 = service name
        |#
        |restartService() {
        |    app_name=$1
        |    service $app_name restart
        |}
        |""".stripMargin,
    "rpm restartService() scriptlet is missing or incorrect"
  )
  ()
}

TaskKey[Unit]("checkSpecAutostart") := {
  val spec = IO.read(target.value / "rpm" / "SPECS" / "rpm-test.spec")
  assert(
    spec contains
      """
        |# Scriptlet syntax: http://fedoraproject.org/wiki/Packaging:ScriptletSnippets#Syntax
        |# $1 == 1 is first installation and $1 == 2 is upgrade
        |if [ $1 -eq 1 ] ;
        |then
        |  addService rpm-test || echo "rpm-test could not be registered"
        |  startService rpm-test || echo "rpm-test could not be started"
        |fi
        |""".stripMargin,
    "rpm rpm addService, startService post install commands missing or incorrect"
  )

  ()
}

TaskKey[Unit]("checkSpecNoAutostart") := {
  val spec = IO.read(target.value / "rpm" / "SPECS" / "rpm-test.spec")
  assert(
    spec contains
      """
        |# Scriptlet syntax: http://fedoraproject.org/wiki/Packaging:ScriptletSnippets#Syntax
        |# $1 == 1 is first installation and $1 == 2 is upgrade
        |if [ $1 -eq 1 ] ;
        |then
        |  addService rpm-test || echo "rpm-test could not be registered"
        |fi
        |""".stripMargin,
    "rpm rpm addService, startService post install commands missing or incorrect"
  )

  ()
}
