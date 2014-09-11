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
  val debianChangelog = SettingKey[Option[File]]("debian-changelog", "The changelog for this deb file")
  // Package building
  val debianNativePackaging = TaskKey[File]("debian-packaging-native", "Builds the debian package with native cli tools")
  val debianJDebPackaging = TaskKey[File]("debian-packaging-jdeb", "Builds the debian package with jdeb (java-based)")

  val debianControlFile = TaskKey[File]("debian-control-file", "Makes the debian package control file.")
  val debianMaintainerScripts = TaskKey[Seq[(File, String)]]("debian-maintainer-scripts", "Makes the debian maintainer scripts.")
  val debianConffilesFile = TaskKey[File]("debian-conffiles-file", "Makes the debian package conffiles file.")
  val debianUpstartFile = TaskKey[File]("debian-upstart-file", "Makes the upstart file for this debian package.")
  val debianLinksfile = TaskKey[File]("debian-links-file", "Makes the debian package links file.")
  val debianMD5sumsFile = TaskKey[File]("debian-md5sums-file", "Makes the debian package md5sums file.")
  val debianZippedMappings = TaskKey[Seq[LinuxPackageMapping]]("debian-zipped-mappings", "Files that need to be gzipped when they hit debian.")
  val debianCombinedMappings = TaskKey[Seq[LinuxPackageMapping]]("debian-combined-mappings", "All the mappings of files for the final package.")
  val debianExplodedPackage = TaskKey[File]("debian-exploded-package", "makes an exploded debian package")
  val lintian = TaskKey[Unit]("lintian", "runs the debian lintian tool on the current package.")
  val debianSign = TaskKey[File]("debian-sign", "runs the dpkg-sig command to sign the generated deb file.")
  val debianSignRole = SettingKey[String]("debian-sign-role", "The role to use when signing a debian file (defaults to 'builder').")
  val genChanges = TaskKey[File]("gen-changes", "runs the dpkg-genchanges command to generate the .changes file.")

  // Debian control scripts
  val debianControlScriptsDirectory = SettingKey[File]("debian-control-scripts-directory",
    "Directory where all debian control scripts reside. Default is 'src/debian/DEBIAN'")
  val debianMakePreinstScript = TaskKey[Option[File]]("makePreinstScript", "Creates or discovers the preinst script used by this project")
  val debianMakePrermScript = TaskKey[Option[File]]("makePrermScript", "Creates or discovers the prerm script used by this project")
  val debianMakePostinstScript = TaskKey[Option[File]]("makePostInstScript", "Creates or discovers the postinst script used by this project")
  val debianMakePostrmScript = TaskKey[Option[File]]("makePostrmScript", "Creates or discovers the postrm script used by this project")
  val debianMakeChownReplacements = TaskKey[(String, String)]("debianMakeChownReplacements", "Creates the chown commands for correct own files and directories")

}

/** Keys used for Debian specific settings. */
object Keys extends DebianKeys {
  // Metadata keys
  def name = sbt.Keys.name
  def packageName = linux.Keys.packageName
  def executableScriptName = linux.Keys.executableScriptName
  def version = sbt.Keys.version
  def maintainer = linux.Keys.maintainer
  def packageArchitecture = linux.Keys.packageArchitecture
  def packageDescription = linux.Keys.packageDescription
  def packageSummary = linux.Keys.packageSummary

  // Package building
  def sourceDirectory = sbt.Keys.sourceDirectory
  def linuxPackageMappings = linux.Keys.linuxPackageMappings
  def linuxPackageSymlinks = linux.Keys.linuxPackageSymlinks
  def packageBin = sbt.Keys.packageBin
  def target = sbt.Keys.target
  def streams = sbt.Keys.streams

  //init script parameters
  def serverLoading = linux.Keys.serverLoading

  val debianPackageInstallSize = TaskKey[Long]("debian-installed-size")
}
