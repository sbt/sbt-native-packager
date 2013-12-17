package com.typesafe.sbt
package packager
package debian

import Keys._
import sbt._
import sbt.Keys.{ mappings, target, name, mainClass, normalizedName }
import linux.LinuxPackageMapping
import linux.LinuxSymlink
import linux.LinuxFileMetaData
import com.typesafe.sbt.packager.Hashing
import com.typesafe.sbt.packager.linux.LinuxSymlink
import com.typesafe.sbt.packager.archetypes.TemplateWriter

trait DebianPlugin extends Plugin with linux.LinuxPlugin {
  val Debian = config("debian") extend Linux

  import com.typesafe.sbt.packager.universal.Archives

  private[this] final def copyAndFixPerms(from: File, to: File, perms: LinuxFileMetaData, zipped: Boolean = false): Unit = {
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

  private[this] def filterAndFixPerms(script: File, replacements: DebianControlScriptReplacements, perms: LinuxFileMetaData): File = {
    val filtered = TemplateWriter.generateScript(script.toURI.toURL, replacements.makeReplacements)
    IO.delete(script)
    IO.write(script, filtered)
    chmod(script, perms.permissions)
    script
  }

  private[this] def scriptMapping(scriptName: String)(script: Option[File], controlDir: File): Seq[(File, String)] = {
    (script, controlDir) match {
      // check if user defined script exists
      case (_, dir) if (dir / scriptName).exists =>
        Seq(file((dir / scriptName).getAbsolutePath) -> scriptName)
      // create mappings for generated script
      case (scr, _) => scr.toSeq.map(_ -> scriptName)
    }
  }


  def debianSettings: Seq[Setting[_]] = Seq(
    debianPriority := "optional",
    debianSection := "java",
    debianPackageDependencies := Seq.empty,
    debianPackageRecommends := Seq.empty,
    debianSignRole := "builder",
    target in Debian <<= (target, name in Debian, version in Debian) apply ((t, n, v) => t / (n + "-" + v)),
    name in Debian <<= (name in Linux),
    version in Debian <<= (version in Linux),
    linuxPackageMappings in Debian <<= linuxPackageMappings,
    packageDescription in Debian <<= packageDescription in Linux,
    packageSummary in Debian <<= packageSummary in Linux,
    maintainer in Debian <<= maintainer in Linux,

    // Debian Control Scripts
    debianControlScriptsReplacements <<= (maintainer in Debian, packageSummary in Debian, normalizedName, version) apply DebianControlScriptReplacements,

    debianControlScriptsDirectory <<= (sourceDirectory) apply (_ / "debian" / "DEBIAN"),
    debianMaintainerScripts := Seq.empty,
    debianMakePreinstScript := None,
    debianMakePrermScript := None,
    debianMakePostinstScript := None,
    debianMakePostrmScript := None,

    debianMaintainerScripts <++= (debianMakePrermScript, debianControlScriptsDirectory) map scriptMapping("prerm"),
    debianMaintainerScripts <++= (debianMakePreinstScript, debianControlScriptsDirectory) map scriptMapping("preinst"),
    debianMaintainerScripts <++= (debianMakePostinstScript, debianControlScriptsDirectory) map scriptMapping("postinst"),
    debianMaintainerScripts <++= (debianMakePostrmScript, debianControlScriptsDirectory) map scriptMapping("postrm")) ++ inConfig(Debian)(Seq(
      packageArchitecture := "all",
      debianPackageInfo <<=
        (name, version, maintainer, packageSummary, packageDescription) apply PackageInfo,
      debianPackageMetadata <<=
        (debianPackageInfo,
          debianPriority, packageArchitecture, debianSection,
          debianPackageDependencies, debianPackageRecommends) apply PackageMetaData,
      debianPackageInstallSize <<= linuxPackageMappings map { mappings =>
        (for {
          LinuxPackageMapping(files, _, zipped) <- mappings
          (file, _) <- files
          if !file.isDirectory && file.exists
          // TODO - If zipped, heuristically figure out a reduction factor.
        } yield file.length).sum / 1024
      },
      debianControlFile <<= (debianPackageMetadata, debianPackageInstallSize, target) map {
        (data, size, dir) =>
          val cfile = dir / "DEBIAN" / "control"
          IO.write(cfile, data.makeContent(size), java.nio.charset.Charset.defaultCharset)
          chmod(cfile, "0644")
          cfile
      },
      debianConffilesFile <<= (linuxPackageMappings, target) map {
        (mappings, dir) =>
          val cfile = dir / "DEBIAN" / "conffiles"
          val conffiles = for {
            LinuxPackageMapping(files, meta, _) <- mappings
            if meta.config != "false"
            (file, name) <- files
            if file.isFile
          } yield name
          IO.writeLines(cfile, conffiles)
          chmod(cfile, "0644")
          cfile
      },
      debianExplodedPackage <<= (linuxPackageMappings, debianControlFile, debianMaintainerScripts, debianConffilesFile, debianControlScriptsReplacements, linuxPackageSymlinks, target, streams)
        map { (mappings, _, maintScripts, _, replacements, symlinks, t, streams) =>

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
            val targetFile = t / "DEBIAN" / name
            copyAndFixPerms(file, targetFile, LinuxFileMetaData())
            filterAndFixPerms(targetFile, replacements, LinuxFileMetaData())
          }

          // Check for non root user/group and append to postinst / postrm
          // filter all root mappings, map to (user,group) key, group by, append everything
          mappings filter {
            case LinuxPackageMapping(_, LinuxFileMetaData("root", "root", _, _, _), _) => false
            case _ => true
          } map {
            case LinuxPackageMapping(paths, LinuxFileMetaData(user, group, _, _, _), _) => (user, group) -> paths
          } groupBy (_._1) foreach {
            case ((user, group), pathList) =>
              streams.log info ("Altering postrm/postinst files to add user " + user + " and group " + group)
              val postinst = t / "DEBIAN" / "postinst"
              val postrm = t / "DEBIAN" / "postrm"

              val replacements = Seq("group" -> group, "user" -> user)
              IO.append(postinst, TemplateWriter.generateScript(DebianPlugin.postinstGroupaddTemplateSource, replacements))
              IO.append(postinst, TemplateWriter.generateScript(DebianPlugin.postinstUseraddTemplateSource, replacements))

              // remove key, flatten it and then go through each file
              pathList.map(_._2).flatten foreach {
                case (file, name) =>
                  val pathReplacements = replacements :+ ("path" -> (file / name).getAbsolutePath)
                  IO.append(postinst, TemplateWriter.generateScript(DebianPlugin.postinstChownTemplateSource, pathReplacements))
              }

              IO.append(postrm, TemplateWriter.generateScript(DebianPlugin.postrmPurgeTemplateSource, replacements))
          }
          t
        },
      debianMD5sumsFile <<= (debianExplodedPackage, target) map {
        (mappings, dir) =>
          val md5file = dir / "DEBIAN" / "md5sums"
          val md5sums = for {
            (file, name) <- (dir.*** --- dir x relativeTo(dir))
            if file.isFile
            if !(name startsWith "DEBIAN")
            if !(name contains "debian-binary")
            // TODO - detect symlinks with Java7 (when we can) rather than hackery...
            if file.getCanonicalPath == file.getAbsolutePath
            fixedName = if (name startsWith "/") name drop 1 else name
          } yield Hashing.md5Sum(file) + "  " + fixedName
          IO.writeLines(md5file, md5sums)
          chmod(md5file, "0644")
          md5file
      },
      packageBin <<= (debianExplodedPackage, debianMD5sumsFile, target, streams) map { (pkgdir, _, tdir, s) =>
        // Make the package.  We put this in fakeroot, so we can build the package with root owning files.
        Process(Seq("fakeroot", "--", "dpkg-deb", "--build", pkgdir.getAbsolutePath), Some(tdir)) ! s.log match {
          case 0 => ()
          case x => sys.error("Failure packaging debian file.  Exit code: " + x)
        }
        file(tdir.getAbsolutePath + ".deb")
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
      }))

}

object DebianPlugin {
  private def postinstGroupaddTemplateSource: java.net.URL = getClass.getResource("postinst-groupadd")
  private def postinstUseraddTemplateSource: java.net.URL = getClass.getResource("postinst-useradd")
  private def postinstChownTemplateSource: java.net.URL = getClass.getResource("postinst-chown")
  private def postrmPurgeTemplateSource: java.net.URL = getClass.getResource("postrm-purge")
}
