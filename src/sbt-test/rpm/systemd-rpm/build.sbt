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

requiredStartFacilities in Rpm := Some("serviceA.service")

TaskKey[Unit]("unzip") <<= (packageBin in Rpm, streams) map { (rpmFile, streams) =>
  val rpmPath = Seq(rpmFile.getAbsolutePath)
  Process("rpm2cpio" , rpmPath) #| Process("cpio -i --make-directories") ! streams.log
  ()
}

TaskKey[Unit]("checkStartupScript") <<= (target, streams) map { (target, out) =>
  val script = IO.read(file("usr/lib/systemd/system/rpm-test.service"))
  val runScript = file("usr/share/rpm-test/bin/rpm-test")
  assert(script.contains("Requires=serviceA.service"), "script doesn't contain Default-Start header\n" + script)
  out.log.success("Successfully tested systemd start up script")
  ()
}

TaskKey[Unit]("checkSpecFile") <<= (target, streams) map { (target, out) =>
  val spec = IO.read(target / "rpm" / "SPECS" / "rpm-test.spec")
  println(spec)
  assert(spec contains
    """
      |#
      |# Adding service to autostart
      |# $1 = service name
      |#
      |startService() {
      |    app_name=$1
      |
      |    app_sys_config="/etc/sysconfig/${app_name}"
      |    [ -e "${app_sys_config}" ] && . "${app_sys_config}"
      |    if [ -n "${PACKAGE_PREFIX}" ] ;
      |    then
      |      default_install_location="/usr/share/rpm-test"
      |      actual_install_location="${PACKAGE_PREFIX}/${app_name}"
      |
      |      sed -i "s|$default_install_location|$actual_install_location|g" "/usr/lib/systemd/system/${app_name}.service"
      |    fi
      |
      |    systemctl enable "$app_name.service"
      |    systemctl start "$app_name.service"
      |}
      |""".stripMargin, "rpm scriptlet does not systemd service registration and startup")

  assert(spec contains
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
      |""".stripMargin, "rpm scriptlet does not systemd stop service and disable")

  assert(spec contains
    """
      |#
      |# Restarting the service after package upgrade
      |# $1 = service name
      |#
      |restartService() {
      |   app_name=$1
      |
      |   systemctl daemon-reload
      |   systemctl try-restart "$app_name.service"
      |}
      |""".stripMargin, "rpm scriptlet does not systemd reload during restart")

  out.log.success("Successfully tested rpm test file")
  ()
}
