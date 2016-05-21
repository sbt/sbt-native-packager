package com.typesafe.sbt
package packager
package linux

import sbt._
import com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader

/** Linux packaging generic build targets. */
trait LinuxKeys {
  val packageArchitecture = SettingKey[String]("package-architecture", "The architecture used for this linux package.")
  val daemonUser = SettingKey[String]("daemon-user", "User to start application daemon")
  val daemonUserUid = SettingKey[Option[String]]("daemon-user-uid", "UID of daemonUser")
  val daemonGroup = SettingKey[String]("daemon-group", "Group to start application daemon")
  val daemonGroupGid = SettingKey[Option[String]]("daemon-group-gid", "GID of daemonGroup")
  val daemonShell = SettingKey[String]("daemon-shell", "Shell provided for the daemon user")

  val linuxPackageMappings = TaskKey[Seq[LinuxPackageMapping]]("linux-package-mappings", "File to install location mappings including owner and privileges.")
  val linuxPackageSymlinks = TaskKey[Seq[LinuxSymlink]]("linux-package-symlinks", "Symlinks we should produce in the underlying package.")
  val generateManPages = TaskKey[Unit]("generate-man-pages", "Shows all the man files in the current project")
  val termTimeout = SettingKey[Int]("term-timeout", "Timeout before sigterm on stop")
  val killTimeout = SettingKey[Int]("kill-timeout", "Timeout before sigkill on stop (after term)")

  val linuxMakeStartScript = TaskKey[Option[File]]("linuxMakeStartScript", "Creates or discovers the start script used by this project")
  val linuxStartScriptTemplate = TaskKey[URL]("linuxStartScriptTemplate", "The location of the template start script file we use for debian (upstart or init.d")
  val linuxStartScriptName = SettingKey[Option[String]]("linuxStartScriptName", "The name of the start script for debian (primary useful for systemd)")
  val linuxEtcDefaultTemplate = TaskKey[URL]("linuxEtcDefaultTemplate", "The location of the /etc/default/<pkg> template script.")
  val linuxScriptReplacements = SettingKey[Seq[(String, String)]](
    "linuxScriptReplacements",
    """|Replacements of template parameters used in linux scripts.
         |  Default supported templates:
         |  execScript - name of the script in /usr/bin
         |  author - author of this project
         |  descr - short description
         |  chdir - execution path of the script
         |  retries - on fail, how often should a restart be tried
         |  retryTimeout - pause between retries
         |  appName - name of application
         |  appClasspath - application classpath
         |  appMainClass - main class to start
         |  daemonUser - daemon user
         |  daemonUserUid - daemon user uid
         |  daemonGroup - daemon group
         |  daemonGroupGid - daemon group gid
         |  termTimeout - timeout before sigterm on stop
         |  killTimeout - timeout before sigkill on stop (after term)
      """.stripMargin
  )

  val makeEtcDefault = TaskKey[Option[File]]("makeEtcDefault", "Creates or discovers the /etc/default/ script")

  val defaultLinuxInstallLocation = SettingKey[String]("defaultLinuxInstallLocation", "The location where we will install generic linux packages.")
  val defaultLinuxLogsLocation = SettingKey[String]("defaultLinuxLogsLocation", "The location where application logs will be stored.")
  val defaultLinuxConfigLocation = SettingKey[String]("defaultLinuxConfigLocation", "The location where application config files will be stored")
  val defaultLinuxStartScriptLocation = SettingKey[String]("defaultLinuxStartScriptLocation", "The location where start script for server application will be stored")

}
