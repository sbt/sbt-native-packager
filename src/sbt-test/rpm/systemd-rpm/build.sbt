import com.typesafe.sbt.packager.Compat._

enablePlugins(JavaServerAppPackaging, SystemdPlugin)

name := "rpm-test"
version := "0.1.0"
maintainer := "Alexey Kardapoltsev <alexey.kardapoltsev@frumatic.com>"

packageSummary := "Test rpm package"
packageDescription := """A fun package description of our software,
  with multiple lines."""

rpmRelease := "1"
rpmVendor := "typesafe"
rpmUrl := Some("http://github.com/sbt/sbt-native-packager")
rpmLicense := Some("BSD")
rpmGroup := Some("test-group")

(Rpm / requiredStartFacilities) := Some("serviceA.service")

TaskKey[Unit]("unzip") := {
  val rpmPath = Seq(((Rpm / packageBin)).value.getAbsolutePath)
  sys.process.Process("rpm2cpio", rpmPath) #| sys.process.Process("cpio -i --make-directories") ! streams.value.log
  ()
}

TaskKey[Unit]("checkStartupScript") := {
  val script = IO.read(file("usr/lib/systemd/system/rpm-test.service"))
  val runScript = file("usr/share/rpm-test/bin/rpm-test")
  assert(script.contains("Requires=serviceA.service"), "script doesn't contain Default-Start header\n" + script)
  assert(script.contains("SuccessExitStatus="), "script doesn't contain SuccessExitStatus header\n" + script)
  streams.value.log.success("Successfully tested systemd start up script")
  ()
}

TaskKey[Unit]("checkSpecFile") := {
  val spec = IO.read(target.value / "rpm" / "SPECS" / "rpm-test.spec")
  println(spec)
  assert(
    spec contains
      """
      |#
      |# Adding service for management
      |# $1 = service name
      |#
      |addService() {
      |    app_name=$1
      |
      |    app_sys_config="/etc/sysconfig/${app_name}"
      |    [ -e "${app_sys_config}" ] && . "${app_sys_config}"
      |    if [ -n "${PACKAGE_PREFIX}" ] ;
      |    then
      |        default_install_location="/usr/share/rpm-test"
      |        actual_install_location="${PACKAGE_PREFIX}/${app_name}"
      |
      |        sed -i "s|$default_install_location|$actual_install_location|g" "/usr/lib/systemd/system/${app_name}.service"
      |    fi
      |
      |    systemctl enable "$app_name.service"
      |}
      |""".stripMargin,
    "rpm addService() scriptlet is missing or incorrect"
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
      |    systemctl start "$app_name.service"
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
      |
      |stopService() {
      |    app_name=$1
      |
      |    systemctl stop "$app_name.service"
      |    systemctl disable "$app_name.service"
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
      |
      |    systemctl daemon-reload
      |    systemctl try-restart "$app_name.service"
      |}
      |""".stripMargin,
    "rpm restartService() scriptlet is missing or incorrect"
  )

  streams.value.log.success("Successfully tested rpm test file")
  ()
}

TaskKey[Unit]("checkSpecAutostart") := {
  val spec = IO.read(target.value / "rpm" / "SPECS" / "rpm-test.spec")
  println(spec)

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
    "rpm addService, startService post install commands missing or incorrect"
  )
  ()
}

TaskKey[Unit]("checkSpecNoAutostart") := {
  val spec = IO.read(target.value / "rpm" / "SPECS" / "rpm-test.spec")
  println(spec)

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
    "rpm addService post install commands missing or incorrect"
  )
  ()
}
