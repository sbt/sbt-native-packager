package com.typesafe.packager.linux

import sbt._

/** Linux packaging generic build targets. */
trait Keys {
  val packageArchitecture = SettingKey[String]("package-architecture", "The architecture used for this linux package.")
  val packageDescription = SettingKey[String]("package-description", "The description of the package.  Used when searching.")
  val maintainer = SettingKey[String]("maintainer", "The name/email address of a maintainer for the native package.")
  val linuxPackageMappings = TaskKey[Seq[LinuxPackageMapping]]("linux-package-mappings", "File to install location mappings including owner and privileges.")
  val generateManPages = TaskKey[Unit]("generate-man-pages", "Shows all the man files in the current project")  
}

object Keys extends Keys {
  def sourceDirectory = sbt.Keys.sourceDirectory
}