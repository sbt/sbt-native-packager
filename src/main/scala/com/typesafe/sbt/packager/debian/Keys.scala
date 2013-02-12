package com.typesafe.sbt
package packager
package debian

import sbt._
import linux.LinuxPackageMapping

/** DEB packaging specifc build targets. */
trait DebianKeys {
  // Metadata keys
  val debianSection = SettingKey[String]("debian-section", "The section category for this deb file.")
  val debianPriority = SettingKey[String]("debian-priority")
  val debianPackageDependencies = SettingKey[Seq[String]]("debian-package-dependencies", "Packages that this debian package depends on.")
  val debianPackageRecommends = SettingKey[Seq[String]]("debian-package-recommends", "Packages recommended to use with the currently packaged one.")
  val debianPackageInfo = SettingKey[PackageInfo]("debian-package-info", "Information (name, version, etc.) about a debian package.")
  val debianPackageMetadata = SettingKey[PackageMetaData]("debian-package-metadata", "Meta data used when constructing a debian package.")
  // Package building
  val debianControlFile = TaskKey[File]("debian-control-file", "Makes the debian package control file.")
  val debianMaintainerScripts = TaskKey[Seq[(File, String)]]("debian-maintainer-scripts", "Makes the debian maintainer scripts.")
  val debianConffilesFile = TaskKey[File]("debian-conffiles-file", "Makes the debian package conffiles file.")
  val debianMD5sumsFile = TaskKey[File]("debian-md5sums-file", "Makes the debian package md5sums file.")
  val debianZippedMappings = TaskKey[Seq[LinuxPackageMapping]]("debian-zipped-mappings", "Files that need to be gzipped when they hit debian.")
  val debianCombinedMappings = TaskKey[Seq[LinuxPackageMapping]]("debian-combined-mappings", "All the mappings of files for the final package.")
  val debianExplodedPackage = TaskKey[File]("debian-exploded-package", "makes an exploded debian package")
  val lintian = TaskKey[Unit]("lintian", "runs the debian lintian tool on the current package.")
  val debianSign = TaskKey[File]("debian-sign", "runs the dpkg-sig command to sign the generated deb file.")
  val debianSignRole = SettingKey[String]("debian-sign-role", "The role to use when signing a debian file (defaults to 'builder').")
}

/** Keys used for RPM specific settings. */
object Keys extends DebianKeys {
  // Metadata keys
  def name = sbt.Keys.name
  def version = sbt.Keys.version
  def maintainer = linux.Keys.maintainer
  def packageArchitecture = linux.Keys.packageArchitecture
  def packageDescription = linux.Keys.packageDescription
  def packageSummary = linux.Keys.packageSummary
  
  // Package building
  def sourceDirectory = sbt.Keys.sourceDirectory
  def linuxPackageMappings = linux.Keys.linuxPackageMappings
  def packageBin = sbt.Keys.packageBin
  def target = sbt.Keys.target
  def streams = sbt.Keys.streams

  val debianPackageInstallSize = TaskKey[Long]("debian-installed-size")
}
