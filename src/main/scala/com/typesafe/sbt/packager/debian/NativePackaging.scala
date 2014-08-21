package com.typesafe.sbt
package packager
package debian

import Keys._
import sbt._
import sbt.Keys.{ target, name, TaskStreams }
import linux.{ LinuxFileMetaData, LinuxPackageMapping, LinuxSymlink }
import linux.Keys.{ linuxScriptReplacements, daemonShell }
import com.typesafe.sbt.packager.Hashing
import com.typesafe.sbt.packager.archetypes.TemplateWriter

/**
 * This provides a dpgk based implementation for debian packaging.
 * Your machine must have dpkg installed to use this.
 *
 * {{
 *    packageBin in Debian <<= debianNativePackaging in Debian
 * }}
 *
 *
 *
 */
trait NativePackaging { this: DebianPlugin with linux.LinuxPlugin =>

  import com.typesafe.sbt.packager.universal.Archives
  import DebianPlugin.Names
  import linux.LinuxPlugin.Users

  private[debian] def debianNativeSettings: Seq[Setting[_]] = Seq(
    genChanges <<= (packageBin, target, debianChangelog, name, version, debianPackageMetadata) map {
      (pkg, tdir, changelog, name, version, data) =>
        changelog match {
          case None => sys.error("Cannot generate .changes file without a changelog")
          case Some(chlog) => {
            // dpkg-genchanges needs a debian "source" directory, different from the DEBIAN "binary" directory
            val debSrc = tdir / "../tmp" / Names.DebianSource
            debSrc.mkdirs()
            copyAndFixPerms(chlog, debSrc / Names.Changelog, LinuxFileMetaData("0644"))
            IO.writeLines(debSrc / Names.Files, List(pkg.getName + " " + data.section + " " + data.priority))
            // dpkg-genchanges needs a "source" control file, located in a "debian" directory
            IO.writeLines(debSrc / Names.Control, List(data.makeSourceControl()))
            val changesFileName = name + "_" + version + "_" + data.architecture + ".changes"
            val changesFile: File = tdir / ".." / changesFileName
            try {
              val changes = Process(Seq("dpkg-genchanges", "-b"), Some(tdir / "../tmp")) !!
              val allChanges = List(changes)
              IO.writeLines(changesFile, allChanges)
            } catch {
              case e: Exception => sys.error("Failure generating changes file." + e.getStackTraceString)
            }
            changesFile
          }
        }

    },
    debianSign <<= (packageBin, debianSignRole, streams) map { (deb, role, s) =>
      Process(Seq("dpkg-sig", "-s", role, deb.getAbsolutePath), Some(deb.getParentFile())) ! s.log match {
        case 0 => ()
        case x => sys.error("Failed to sign debian package! exit code: " + x)
      }
      deb
    },
    lintian <<= packageBin map { file =>
      Process(Seq("lintian", "-c", "-v", file.getName), Some(file.getParentFile)).!
    },

    /** Implementation of the actual packaging  */
    debianNativePackaging <<= (debianExplodedPackage, debianMD5sumsFile, debianSection, debianPriority, name, version, packageArchitecture, target, streams) map {
      (pkgdir, _, section, priority, name, version, arch, tdir, s) =>
        s.log.info("Building debian package with native implementation")
        // Make the package.  We put this in fakeroot, so we can build the package with root owning files.
        val archive = archiveFilename(name, version, arch)
        Process(Seq("fakeroot", "--", "dpkg-deb", "--build", pkgdir.getAbsolutePath, "../" + archive), Some(tdir)) ! s.log match {
          case 0 => ()
          case x => sys.error("Failure packaging debian file.  Exit code: " + x)
        }
        tdir / ".." / archive
    }
  )

}

/**
 * This provides the task for building a debian packaging with
 * native tools
 *
 */
object Native {

  /* static assets definitions */

  private[debian] def postinstGroupaddTemplateSource: java.net.URL = getClass.getResource("postinst-groupadd")
  private[debian] def postinstUseraddTemplateSource: java.net.URL = getClass.getResource("postinst-useradd")
  private[debian] def postinstChownTemplateSource: java.net.URL = getClass.getResource("postinst-chown")
  private[debian] def postrmPurgeTemplateSource: java.net.URL = getClass.getResource("postrm-purge")
  private[debian] def headerSource: java.net.URL = getClass.getResource("header")
}