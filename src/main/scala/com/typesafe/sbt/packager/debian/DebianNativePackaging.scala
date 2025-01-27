package com.typesafe.sbt.packager
package debian

import com.typesafe.sbt.SbtNativePackager.Debian
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.linux.LinuxFileMetaData
import com.typesafe.sbt.packager.Compat._
import sbt.Keys.*
import sbt.{*, given}
import xsbti.FileConverter

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
  private[debian] def debianNativeSettings: Seq[Setting[?]] =
    inConfig(Debian)(
      Seq(
        debianNativeBuildOptions += "-Znone", // packages are largely JARs, which are already compressed
        genChanges := {
          val conv0 = fileConverter.value
          implicit val conv: FileConverter = conv0
          dpkgGenChanges(packageBin.value, debianChangelog.value, debianPackageMetadata.value, target.value)
        },
        debianSign := {
          val conv0 = fileConverter.value
          implicit val conv: FileConverter = conv0
          val deb = packageBin.value
          val debFile = PluginCompat.toFile(deb)
          val role = debianSignRole.value
          val log = streams.value.log
          sys.process
            .Process(Seq("dpkg-sig", "-s", role, debFile.getAbsolutePath), Some(debFile.getParentFile)) ! log match {
            case 0 => ()
            case x =>
              sys.error("Failed to sign debian package! exit code: " + x)
          }
          deb
        },
        lintian := {
          val conv0 = fileConverter.value
          implicit val conv: FileConverter = conv0
          val deb = packageBin.value
          val debFile = PluginCompat.toFile(deb)
          sys.process.Process(Seq("lintian", "-c", "-v", debFile.getName), Some(debFile.getParentFile)).!
        },
        /** Implementation of the actual packaging */
        packageBin := buildPackage(
          name.value,
          version.value,
          packageArchitecture.value,
          stage.value,
          debianNativeBuildOptions.value,
          fileConverter.value,
          streams.value.log
        )
      )
    )

  private[this] def dpkgGenChanges(
    debFile: PluginCompat.FileRef,
    changelog: Option[File],
    data: PackageMetaData,
    targetDir: File
  )(implicit conv: FileConverter): PluginCompat.FileRef = {
    println(s"Changelog: $changelog")
    changelog match {
      case None =>
        sys.error("Cannot generate .changes file without a changelog")
      case Some(chlog) =>
        // dpkg-genchanges needs a debian "source" directory, different from the DEBIAN "binary" directory
        val debSrc = targetDir / "../tmp" / Names.DebianSource
        debSrc.mkdirs()
        copyAndFixPerms(chlog, debSrc / Names.Changelog, LinuxFileMetaData("0644"))
        val debFileFile = PluginCompat.toFile(debFile)
        IO.writeLines(debSrc / Names.Files, List(debFileFile.getName + " " + data.section + " " + data.priority))
        // dpkg-genchanges needs a "source" control file, located in a "debian" directory
        IO.writeLines(debSrc / Names.Control, List(data.makeSourceControl()))
        val changesFileName = debFileFile.getName.replaceAll("deb$", "changes")
        val changesFile: File = targetDir / ".." / changesFileName
        try {
          val changes = sys.process.Process(Seq("dpkg-genchanges", "-b"), Some(targetDir / "../tmp")).!!
          val allChanges = List(changes)
          IO.writeLines(changesFile, allChanges)
        } catch {
          case e: Exception =>
            throw new RuntimeException("Failure generating changes file.", e)
        }
        val changesFileRef = PluginCompat.toFileRef(changesFile)
        changesFileRef
    }
  }

  private[this] def buildPackage(
    name: String,
    version: String,
    arch: String,
    stageDir: File,
    buildOptions: Seq[String],
    conv0: FileConverter,
    log: Logger
  ): PluginCompat.FileRef = {
    implicit val conv: FileConverter = conv0
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
    val out = stageDir / ".." / archive
    PluginCompat.toFileRef(out)
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
