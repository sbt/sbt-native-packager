package com.typesafe.sbt.packager.debian

import com.typesafe.sbt.SbtNativePackager.Debian
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.linux.LinuxFileMetaData
import com.typesafe.sbt.packager.Compat._
import sbt.Keys._
import sbt._

/**
  * ==Native Packaging==
  *
  * This provides a dpkg based implementation for debian packaging.
  *
  * ==Requirements==
  *
  * You need the debian dpkg toolchain installed. This includes
  *   - fakeroot
  *   - dpkg-deb
  *   - dpkg-genchanges
  *
  * @example
  *   Enable the plugin in the `build.sbt`
  *   {{{
  *    enablePlugins(DebianNativePackaging)
  *   }}}
  */
trait DebianNativePackaging extends DebianPluginLike {

  import DebianPlugin.Names

  /**
    * Using the native installed dpkg-build tools to build the debian package.
    */
  private[debian] def debianNativeSettings: Seq[Setting[_]] =
    inConfig(Debian)(
      Seq(
        debianNativeBuildOptions += "-Znone", // packages are largely JARs, which are already compressed
        genChanges := dpkgGenChanges(
          packageBin.value,
          debianChangelog.value,
          debianPackageMetadata.value,
          target.value
        ),
        debianSign := {
          val deb = packageBin.value
          val role = debianSignRole.value
          val log = streams.value.log
          sys.process.Process(Seq("dpkg-sig", "-s", role, deb.getAbsolutePath), Some(deb.getParentFile)) ! log match {
            case 0 => ()
            case x =>
              sys.error("Failed to sign debian package! exit code: " + x)
          }
          deb
        },
        lintian := {
          val file = packageBin.value
          sys.process.Process(Seq("lintian", "-c", "-v", file.getName), Some(file.getParentFile)).!
        },
        /** Implementation of the actual packaging */
        packageBin := buildPackage(
          name.value,
          version.value,
          packageArchitecture.value,
          stage.value,
          debianNativeBuildOptions.value,
          streams.value.log
        )
      )
    )

  private[this] def dpkgGenChanges(debFile: File, changelog: Option[File], data: PackageMetaData, targetDir: File) = {
    println(s"Changelog: $changelog")
    changelog match {
      case None =>
        sys.error("Cannot generate .changes file without a changelog")
      case Some(chlog) =>
        // dpkg-genchanges needs a debian "source" directory, different from the DEBIAN "binary" directory
        val debSrc = targetDir / "../tmp" / Names.DebianSource
        debSrc.mkdirs()
        copyAndFixPerms(chlog, debSrc / Names.Changelog, LinuxFileMetaData("0644"))
        IO.writeLines(debSrc / Names.Files, List(debFile.getName + " " + data.section + " " + data.priority))
        // dpkg-genchanges needs a "source" control file, located in a "debian" directory
        IO.writeLines(debSrc / Names.Control, List(data.makeSourceControl()))
        val changesFileName = debFile.getName.replaceAll("deb$", "changes")
        val changesFile: File = targetDir / ".." / changesFileName
        try {
          val changes = sys.process.Process(Seq("dpkg-genchanges", "-b"), Some(targetDir / "../tmp")).!!
          val allChanges = List(changes)
          IO.writeLines(changesFile, allChanges)
        } catch {
          case e: Exception =>
            sys.error("Failure generating changes file." + e.getStackTraceString)
        }
        changesFile
    }
  }

  private[this] def buildPackage(
    name: String,
    version: String,
    arch: String,
    stageDir: File,
    buildOptions: Seq[String],
    log: Logger
  ) = {
    log.info("Building debian package with native implementation")
    // Make the package.  We put this in fakeroot, so we can build the package with root owning files.
    val archive = archiveFilename(name, version, arch)
    sys.process.Process(
      Seq("fakeroot", "--", "dpkg-deb", "--build") ++ buildOptions ++ Seq(stageDir.getAbsolutePath, "../" + archive),
      Some(stageDir)
    ) ! log match {
      case 0 => ()
      case x =>
        sys.error("Failure packaging debian file.  Exit code: " + x)
    }
    stageDir / ".." / archive
  }

}

object DebianNativePackaging {

  private[debian] def postinstGroupaddTemplateSource: java.net.URL =
    getClass.getResource("postinst-groupadd")
  private[debian] def postinstUseraddTemplateSource: java.net.URL =
    getClass.getResource("postinst-useradd")
  private[debian] def postinstChownTemplateSource: java.net.URL =
    getClass.getResource("postinst-chown")
  private[debian] def postrmPurgeTemplateSource: java.net.URL =
    getClass.getResource("postrm-purge")
  private[debian] def headerSource: java.net.URL =
    getClass.getResource("header")
}
