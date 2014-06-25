package com.typesafe.sbt
package packager
package debian

import Keys._
import sbt._
import sbt.Keys.{ target, name, normalizedName, TaskStreams }
import linux.{ LinuxFileMetaData, LinuxPackageMapping, LinuxSymlink }
import linux.Keys.{ linuxScriptReplacements, daemonShell }
import com.typesafe.sbt.packager.Hashing
import com.typesafe.sbt.packager.archetypes.TemplateWriter

trait NativePackaging { this: DebianPlugin with linux.LinuxPlugin =>

  import com.typesafe.sbt.packager.universal.Archives
  import DebianPlugin.Names
  import linux.LinuxPlugin.Users

  private[debian] def debianNativeSettings: Seq[Setting[_]] = Seq(
    debianExplodedPackage <<= (linuxPackageMappings, debianControlFile, debianMaintainerScripts, debianConffilesFile, debianChangelog, daemonShell in Linux, linuxScriptReplacements, linuxPackageSymlinks, target, streams)
      map { (mappings, _, maintScripts, _, changelog, shell, replacements, symlinks, t, streams) =>

        // Create files and directories
        mappings foreach {
          case LinuxPackageMapping(paths, perms, zipped) =>
            val (dirs, files) = paths.partition(_._1.isDirectory)
            dirs map {
              case (_, name) => t / name
            } foreach { targetDir =>
              targetDir mkdirs ()
              chmod(targetDir, perms.permissions)
            }

            files map {
              case (file, name) => (file, t / name)
            } foreach {
              case (source, target) => copyAndFixPerms(source, target, perms, zipped)
            }
        }
        // Now generate relative symlinks
        LinuxSymlink.makeSymLinks(symlinks, t, false)

        // Put the maintainer files in `dir / "DEBIAN"` named as specified.
        // Valid values for the name are preinst,postinst,prerm,postrm
        for ((file, name) <- maintScripts) {
          val targetFile = t / Names.Debian / name
          copyAndFixPerms(file, targetFile, LinuxFileMetaData())
          filterAndFixPerms(targetFile, replacements, LinuxFileMetaData())
        }

        // Check for non root user/group and append to postinst / postrm
        // filter all root mappings, map to (user,group) key, group by, append everything
        mappings filter {
          case LinuxPackageMapping(_, LinuxFileMetaData(Users.Root, Users.Root, _, _, _), _) => false
          case _ => true
        } map {
          case LinuxPackageMapping(paths, LinuxFileMetaData(user, group, _, _, _), _) => (user, group) -> paths
        } groupBy (_._1) foreach {
          case ((user, group), pathList) =>
            streams.log info ("Altering postrm/postinst files to add user " + user + " and group " + group)
            val postinst = createFileIfRequired(t / Names.Debian / Names.Postinst, LinuxFileMetaData())
            val postrm = createFileIfRequired(t / Names.Debian / Names.Postrm, LinuxFileMetaData())
            val prerm = createFileIfRequired(t / Names.Debian / Names.Prerm, LinuxFileMetaData())
            val headerScript = IO.readLinesURL(Native.headerSource)

            val replacements = Seq("group" -> group, "user" -> user, "shell" -> shell)

            prependAndFixPerms(prerm, headerScript, LinuxFileMetaData())

            // remove key, flatten it and then go through each file
            pathList.map(_._2).flatten foreach {
              case (_, target) =>
                val pathReplacements = replacements :+ ("path" -> target.toString)
                val chownAdd = Seq(TemplateWriter.generateScript(Native.postinstChownTemplateSource, pathReplacements))
                prependAndFixPerms(postinst, chownAdd, LinuxFileMetaData())
            }

            validateUserGroupNames(user, streams)
            validateUserGroupNames(group, streams)

            val userGroupAdd = Seq(
              TemplateWriter.generateScript(Native.postinstGroupaddTemplateSource, replacements),
              TemplateWriter.generateScript(Native.postinstUseraddTemplateSource, replacements))

            prependAndFixPerms(postinst, userGroupAdd, LinuxFileMetaData())
            prependAndFixPerms(postinst, headerScript, LinuxFileMetaData())

            val purgeAdd = Seq(TemplateWriter.generateScript(Native.postrmPurgeTemplateSource, replacements))
            appendAndFixPerms(postrm, purgeAdd, LinuxFileMetaData())
            prependAndFixPerms(postrm, headerScript, LinuxFileMetaData())
        }
        t
      },
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
    }
  )

  private final def copyAndFixPerms(from: File, to: File, perms: LinuxFileMetaData, zipped: Boolean = false): Unit = {
    if (zipped) {
      IO.withTemporaryDirectory { dir =>
        val tmp = dir / from.getName
        IO.copyFile(from, tmp)
        val zipped = Archives.gzip(tmp)
        IO.copyFile(zipped, to, true)
      }
    } else IO.copyFile(from, to, true)
    // If we have a directory, we need to alter the perms.
    chmod(to, perms.permissions)
    // TODO - Can we do anything about user/group ownership?
  }

  private final def filterAndFixPerms(script: File, replacements: Seq[(String, String)], perms: LinuxFileMetaData): File = {
    val filtered = TemplateWriter.generateScript(script.toURI.toURL, replacements)
    IO.delete(script)
    IO.write(script, filtered)
    chmod(script, perms.permissions)
    script
  }

  private final def prependAndFixPerms(script: File, lines: Seq[String], perms: LinuxFileMetaData): File = {
    val old = IO.readLines(script)
    IO.writeLines(script, lines ++ old, append = false)
    chmod(script, perms.permissions)
    script
  }

  private final def appendAndFixPerms(script: File, lines: Seq[String], perms: LinuxFileMetaData): File = {
    IO.writeLines(script, lines, append = true)
    chmod(script, perms.permissions)
    script
  }

  private final def createFileIfRequired(script: File, perms: LinuxFileMetaData): File = {
    if (!script.exists()) {
      script.createNewFile()
      chmod(script, perms.permissions)
    }
    script
  }

  private final def validateUserGroupNames(user: String, streams: TaskStreams) {
    if ((UserNamePattern findFirstIn user).isEmpty) {
      streams.log.warn("The user or group '" + user + "' may contain invalid characters for Debian based distributions")
    }
    if (user.length > 32) {
      streams.log.warn("The length of '" + user + "' must be not be greater than 32 characters for Debian based distributions.")
    }
  }

}

/**
 * This provides the task for building a debian packaging with
 * native tools
 *
 */
object Native {

  /**
   * The plugin needs to mixin the NativePackaging trait to make this
   * task definition work.
   *
   * {{
   *    packageBin in Debian <<= Native()
   * }}
   */
  def apply(): Def.Initialize[Task[java.io.File]] =
    (debianExplodedPackage, debianMD5sumsFile, debianSection, debianPriority, name, version, packageArchitecture, target, streams) map {
      (pkgdir, _, section, priority, name, version, arch, tdir, s) =>
        // Make the package.  We put this in fakeroot, so we can build the package with root owning files.
        val archive = name + "_" + version + "_" + arch + ".deb"
        Process(Seq("fakeroot", "--", "dpkg-deb", "--build", pkgdir.getAbsolutePath, "../" + archive), Some(tdir)) ! s.log match {
          case 0 => ()
          case x => sys.error("Failure packaging debian file.  Exit code: " + x)
        }
        tdir / ".." / archive
    }

  /* static assets definitions */

  private[debian] def postinstGroupaddTemplateSource: java.net.URL = getClass.getResource("postinst-groupadd")
  private[debian] def postinstUseraddTemplateSource: java.net.URL = getClass.getResource("postinst-useradd")
  private[debian] def postinstChownTemplateSource: java.net.URL = getClass.getResource("postinst-chown")
  private[debian] def postrmPurgeTemplateSource: java.net.URL = getClass.getResource("postrm-purge")
  private[debian] def headerSource: java.net.URL = getClass.getResource("header")
}