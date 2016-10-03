package com.typesafe.sbt
package packager
package debian

import sbt._
import sbt.Keys.{name, packageBin, streams, target, version}
import packager.Hashing
import packager.archetypes.TemplateWriter
import linux.{LinuxFileMetaData, LinuxPackageMapping, LinuxSymlink}
import linux.LinuxPlugin.autoImport.packageArchitecture

import DebianPlugin.autoImport._

/**
  * == Native Packaging ==
  *
  * This provides a dpgk based implementation for debian packaging.
  *
  * == Requirements ==
  *
  * You need the debian dpkg toolchain installed. This includes
  * <ul>
  * <li>fakeroot</li>
  * <li>dpkg-deb</li>
  * <li>dpkg-genchanges</li>
  * </ul>
  *
  *
  * @example Enable the plugin in the `build.sbt`
  * {{{
  *    enablePlugins(DebianNativePackaging)
  * }}}
  *
  */
trait DebianNativePackaging extends DebianPluginLike {

  import com.typesafe.sbt.packager.universal.Archives
  import DebianPlugin.Names
  import linux.LinuxPlugin.Users

  /**
    * Using the native installed dpkg-build tools to build the debian
    * package.
    */
  private[debian] def debianNativeSettings: Seq[Setting[_]] =
    inConfig(Debian)(
      Seq(
        debianNativeBuildOptions += "-Znone", // packages are largely JARs, which are already compressed
        genChanges <<= (packageBin, target, debianChangelog, name, version, debianPackageMetadata) map {
          (pkg, tdir, changelog, name, version, data) =>
            changelog match {
              case None =>
                sys.error("Cannot generate .changes file without a changelog")
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
                  case e: Exception =>
                    sys.error("Failure generating changes file." + e.getStackTraceString)
                }
                changesFile
              }
            }

        },
        debianSign <<= (packageBin, debianSignRole, streams) map { (deb, role, s) =>
          Process(Seq("dpkg-sig", "-s", role, deb.getAbsolutePath), Some(deb.getParentFile())) ! s.log match {
            case 0 => ()
            case x =>
              sys.error("Failed to sign debian package! exit code: " + x)
          }
          deb
        },
        lintian <<= packageBin map { file =>
          Process(Seq("lintian", "-c", "-v", file.getName), Some(file.getParentFile)).!
        },
        /** Implementation of the actual packaging  */
        packageBin <<= (debianExplodedPackage,
                        debianMD5sumsFile,
                        debianSection,
                        debianPriority,
                        name,
                        version,
                        packageArchitecture,
                        debianNativeBuildOptions,
                        target,
                        streams) map { (pkgdir, _, section, priority, name, version, arch, options, tdir, s) =>
          s.log.info("Building debian package with native implementation")
          // Make the package.  We put this in fakeroot, so we can build the package with root owning files.
          val archive = archiveFilename(name, version, arch)
          Process(
            Seq("fakeroot", "--", "dpkg-deb", "--build") ++ options ++ Seq(pkgdir.getAbsolutePath, "../" + archive),
            Some(tdir)
          ) ! s.log match {
            case 0 => ()
            case x =>
              sys.error("Failure packaging debian file.  Exit code: " + x)
          }
          tdir / ".." / archive
        }
      )
    )

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
