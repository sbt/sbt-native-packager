package com.typesafe.sbt
package packager
package linux

import sbt._
import com.typesafe.sbt.packager.archetypes.ServerLoader.ServerLoader

/** Linux packaging generic build targets. */
trait Keys {
  val packageArchitecture = SettingKey[String]("package-architecture", "The architecture used for this linux package.")
  val packageSummary = SettingKey[String]("package-summary", "Summary of the contents of a linux package.")
  val packageDescription = SettingKey[String]("package-description", "The description of the package.  Used when searching.")
  val maintainer = SettingKey[String]("maintainer", "The name/email address of a maintainer for the native package.")
  val daemonUser = SettingKey[String]("daemon-user", "User to start application daemon")
  val serverLoading = SettingKey[ServerLoader]("server-loader", "Loading system to be used for application start script")
  val linuxPackageMappings = TaskKey[Seq[LinuxPackageMapping]]("linux-package-mappings", "File to install location mappings including owner and privileges.")
  val linuxPackageSymlinks = TaskKey[Seq[LinuxSymlink]]("linux-package-symlinks", "Symlinks we should produce in the underlying package.")
  val generateManPages = TaskKey[Unit]("generate-man-pages", "Shows all the man files in the current project")  
}

object Keys extends Keys {
  def sourceDirectory = sbt.Keys.sourceDirectory
}