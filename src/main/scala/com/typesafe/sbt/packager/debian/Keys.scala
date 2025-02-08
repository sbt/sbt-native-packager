package com.typesafe.sbt
package packager
package debian

import sbt.{*, given}
import linux.LinuxPackageMapping

/** DEB packaging specific build targets. */
trait DebianKeys {
  // Metadata keys
  val debianSection = SettingKey[String]("debian-section", "The section category for this deb file.")
  val debianPriority = SettingKey[String]("debian-priority")
  val debianPackageConflicts =
    SettingKey[Seq[String]]("debian-package-conflicts", "Packages that conflict with the currently packaged one.")
  val debianPackageDependencies =
    SettingKey[Seq[String]]("debian-package-dependencies", "Packages that this debian package depends on.")
  val debianPackageProvides =
    SettingKey[Seq[String]]("debian-package-provides", "Packages that are provided by the currently packaged one.")
  val debianPackageRecommends =
    SettingKey[Seq[String]]("debian-package-recommends", "Packages recommended to use with the currently packaged one.")
  val debianPackageInfo =
    SettingKey[PackageInfo]("debian-package-info", "Information (name, version, etc.) about a debian package.")
  val debianPackageMetadata =
    SettingKey[PackageMetaData]("debian-package-metadata", "Meta data used when constructing a debian package.")
  val debianChangelog = SettingKey[Option[File]]("debian-changelog", "The changelog for this deb file")
  // Package building

  val debianControlFile = TaskKey[File]("debian-control-file", "Makes the debian package control file.")

  @deprecated("Use generic maintainerScript task instead", "1.0.3")
  val debianMaintainerScripts =
    TaskKey[Seq[(File, String)]]("debian-maintainer-scripts", "Makes the debian maintainer scripts.")
  val debianConffilesFile = TaskKey[File]("debian-conffiles-file", "Makes the debian package conffiles file.")
  val debianUpstartFile = TaskKey[File]("debian-upstart-file", "Makes the upstart file for this debian package.")
  val debianLinksfile =
    TaskKey[File]("debian-links-file", "Makes the debian package links file.")
  val debianMD5sumsFile = TaskKey[File]("debian-md5sums-file", "Makes the debian package md5sums file.")
  val debianZippedMappings =
    TaskKey[Seq[LinuxPackageMapping]]("debian-zipped-mappings", "Files that need to be gzipped when they hit debian.")
  val debianCombinedMappings =
    TaskKey[Seq[LinuxPackageMapping]]("debian-combined-mappings", "All the mappings of files for the final package.")

  @deprecated("Use Debian/stage instead", "1.2.0")
  val debianExplodedPackage = TaskKey[File]("debian-exploded-package", "makes an exploded debian package")
  val lintian = TaskKey[Unit]("lintian", "runs the debian lintian tool on the current package.")
  val debianSign =
    taskKey[PluginCompat.FileRef]("runs the dpkg-sig command to sign the generated deb file.")
  val debianSignRole =
    SettingKey[String]("debian-sign-role", "The role to use when signing a debian file (defaults to 'builder').")
  val genChanges =
    taskKey[PluginCompat.FileRef]("runs the dpkg-genchanges command to generate the .changes file.")

  // Debian control scripts
  val debianControlScriptsDirectory = SettingKey[File](
    "debian-control-scripts-directory",
    "Directory where all debian control scripts reside. Default is 'src/debian/DEBIAN'"
  )
  @deprecated("Use generic maintainerScript task instead", "1.0.3")
  val debianMakePreinstScript =
    TaskKey[Option[File]]("makePreinstScript", "Creates or discovers the preinst script used by this project")
  @deprecated("Use generic maintainerScript task instead", "1.0.3")
  val debianMakePrermScript =
    TaskKey[Option[File]]("makePrermScript", "Creates or discovers the prerm script used by this project")
  @deprecated("Use generic maintainerScript task instead", "1.0.3")
  val debianMakePostinstScript =
    TaskKey[Option[File]]("makePostInstScript", "Creates or discovers the postinst script used by this project")
  @deprecated("Use generic maintainerScript task instead", "1.0.3")
  val debianMakePostrmScript =
    TaskKey[Option[File]]("makePostrmScript", "Creates or discovers the postrm script used by this project")
  val debianMakeChownReplacements = TaskKey[(String, String)](
    "debianMakeChownReplacements",
    "Creates the chown commands for correct own files and directories"
  )

  val debianPackageInstallSize = TaskKey[Long]("debian-installed-size")

  val debianNativeBuildOptions =
    SettingKey[Seq[String]]("debian-native-build-options", "Options passed to dpkg-deb, e.g. compression level")
}
