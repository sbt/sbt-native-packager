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
import java.io.{ File => JFile }

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

  private[this] def scriptMapping(scriptName: String)(script: Option[JFile], controlDir: JFile): Seq[(File, String)] = {
    (script, controlDir) match {
      case (Some(script), _) => Seq(script -> scriptName)
      case (None, dir) =>
        val script = dir / scriptName
        if (script exists) Seq(file(script getAbsolutePath) -> scriptName) else Seq.empty
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
    debianControlScriptsDirectory := (sourceDirectory.value / "debian" / "DEBIAN"),
    debianMaintainerScripts := Seq.empty,
    debianMakePreinstScript := None,
    debianMakePrermScript := None,
    debianMakePostinstScript := None,
    debianMakePostrmScript := None,

    // TODO - We should make sure there isn't one already specified...
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
      /*debianLinksfile <<= (name, linuxPackageSymlinks, target) map { (name, symlinks, dir) =>
      val lfile = dir / "DEBIAN" / (name + ".links")
      val content =
        for {
          LinuxSymlink(link, destination) <- symlinks
        } yield link + "   " + destination
      IO.writeLines(lfile, content)
      chmod(lfile, "0644")
      lfile
    },*/
      debianExplodedPackage <<= (linuxPackageMappings, debianControlFile, debianMaintainerScripts, debianConffilesFile, linuxPackageSymlinks, target) map { (mappings, _, maintScripts, _, symlinks, t) =>
        // First Create directories, in case we have any without files in them.
        for {
          LinuxPackageMapping(files, perms, zipped) <- mappings
          (file, name) <- files
          if file.isDirectory
          tfile = t / name
          if !tfile.exists
        } tfile.mkdirs()
        for {
          LinuxPackageMapping(files, perms, zipped) <- mappings
          (file, name) <- files
          if !file.isDirectory && file.exists
          tfile = t / name
        } copyAndFixPerms(file, tfile, perms, zipped)

        // Now generate relative symlinks
        LinuxSymlink.makeSymLinks(symlinks, t, false)

        // TODO: Fix this ugly hack to permission directories correctly!
        for {
          file <- (t.***).get
          if file.isDirectory
          if file.getCanonicalPath == file.getAbsolutePath // Ignore symlinks.
        } chmod(file, "0755")
        // Put the maintainer files in `dir / "DEBIAN"` named as specified.
        // Valid values for the name are preinst,postinst,prerm,postrm
        for ((file, name) <- maintScripts) copyAndFixPerms(file, t / "DEBIAN" / name, LinuxFileMetaData())

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
            // TODO - detect symlinks...
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
