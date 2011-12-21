package com.typesafe.packager
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
  val debianPackageMetadata = SettingKey[PackageMetaData]("debian-package-metadata", "Meta data used when constructing a debian package.")
  // Package building
  val debianControlFile = TaskKey[File]("debian-control-file", "Makes the debian package control file.")
  val debianZippedMappings = TaskKey[Seq[LinuxPackageMapping]]("debian-zipped-mappings", "Files that need to be gzipped when they hit debian.")
  val debianCombinedMappings = TaskKey[Seq[LinuxPackageMapping]]("debian-combined-mappings", "All the mappings of files for the final package.")
  val debianExplodedPackage = TaskKey[File]("debian-exploded-package", "makes an exploded debian package")
  val lintian = TaskKey[Unit]("lintian", "runs the debian lintian tool on the current package.")
}

/** Keys used for RPM specific settings. */
object Keys extends DebianKeys {
  // Metadata keys
  def name = sbt.Keys.name
  def version = sbt.Keys.version
  def maintainer = linux.Keys.maintainer
  def packageArchitecture = linux.Keys.packageArchitecture
  def packageDescription = linux.Keys.packageDescription
  
  // Package building
  def sourceDirectory = sbt.Keys.sourceDirectory
  def linuxPackageMappings = linux.Keys.linuxPackageMappings
  def packageBin = sbt.Keys.packageBin
  def target = sbt.Keys.target
}