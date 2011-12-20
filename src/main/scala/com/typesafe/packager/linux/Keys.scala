package com.typesafe.packager.linux

import sbt._

object Keys {
  val maintainer = SettingKey[String]("maintainer", "The name/email address of a maintainer for the native package.")
  val linuxPackageMappings = TaskKey[Seq[LinuxPackageMapping]]("linux-package-mappings", "File to install location mappings including owner and privileges.")
}