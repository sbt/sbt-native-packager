package com.typesafe.sbt
package packager
package linux

import sbt._
import com.typesafe.sbt.packager.archetypes.ServerLoader.ServerLoader
import com.typesafe.sbt.packager.archetypes.JavaAppStartScriptBuilder

/** Linux packaging generic build targets. */
trait Keys {
  val packageArchitecture = SettingKey[String]("package-architecture", "The architecture used for this linux package.")
  val packageSummary = SettingKey[String]("package-summary", "Summary of the contents of a linux package.")
  val packageDescription = SettingKey[String]("package-description", "The description of the package.  Used when searching.")
  val maintainer = SettingKey[String]("maintainer", "The name/email address of a maintainer for the native package.")
  val daemonUser = SettingKey[String]("daemon-user", "User to start application daemon")
  val daemonGroup = SettingKey[String]("daemon-group", "Group to start application daemon")
  val daemonShell = SettingKey[String]("daemon-shell", "Shell provided for the daemon user")
  val serverLoading = SettingKey[ServerLoader]("server-loader", "Loading system to be used for application start script")
  val startRunlevels = SettingKey[String]("start-runlevels", "Sequence of runlevels on which application will start up")
  val stopRunlevels = SettingKey[String]("stop-runlevels", "Sequence of runlevels on which application will stop")
  val requiredStartFacilities = SettingKey[String]("required-start-facilities", "Names of system services that should be provided at application start")
  val requiredStopFacilities = SettingKey[String]("required-stop-facilities", "Names of system services that should be provided at application stop")
  val linuxPackageMappings = TaskKey[Seq[LinuxPackageMapping]]("linux-package-mappings", "File to install location mappings including owner and privileges.")
  val linuxPackageSymlinks = TaskKey[Seq[LinuxSymlink]]("linux-package-symlinks", "Symlinks we should produce in the underlying package.")
  val generateManPages = TaskKey[Unit]("generate-man-pages", "Shows all the man files in the current project")

  val linuxMakeStartScript = TaskKey[Option[File]]("makeStartScript", "Creates or discovers the start script used by this project")
  val linuxStartScriptTemplate = TaskKey[URL]("linuxStartScriptTemplate", "The location of the template start script file we use for debian (upstart or init.d")
  val linuxEtcDefaultTemplate = TaskKey[URL]("linuxEtcDefaultTemplate", "The location of the /etc/default/<pkg> template script.")
  val linuxJavaAppStartScriptBuilder = SettingKey[JavaAppStartScriptBuilder]("linuxJavaAppStartScriptBuilder", "Responsible for loading the start scripts. Only used with archetype.java_server")
  val linuxScriptReplacements = SettingKey[Seq[(String, String)]]("linuxScriptReplacements",
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
      """.stripMargin)

  val makeEtcDefault = TaskKey[Option[File]]("makeEtcDefault", "Creates or discovers the /etc/default/ script")
}

object Keys extends Keys {
  def sourceDirectory = sbt.Keys.sourceDirectory
}