package com.typesafe.sbt
package packager
package debian

import Keys._
import sbt._
import linux.LinuxPackageMapping
import linux.LinuxFileMetaData
import com.typesafe.sbt.packager.Hashing

trait DebianPlugin extends Plugin with linux.LinuxPlugin {
  val Debian = config("debian") extend Linux
  
  private[this] final def copyAndFixPerms(from: File, to: File, perms: LinuxFileMetaData, zipped: Boolean = false): Unit = {
    if(zipped) IO.gzip(from, to)
    else IO.copyFile(from, to, true)
    // If we have a directory, we need to alter the perms.
    chmod(to, perms.permissions)
    // TODO - Can we do anything about user/group ownership?
  }
  
  private[this] final def chmod(file: File, perms: String): Unit =
    Process(Seq("chmod",  perms, file.getAbsolutePath)).! match {
      case 0 => ()
      case n => sys.error("Error running chmod " + perms + " " + file)
    }
  
  def debianSettings: Seq[Setting[_]] = Seq(
    debianPriority := "optional",
    debianSection := "java",
    debianPackageDependencies := Seq.empty,
    debianPackageRecommends := Seq.empty,
    debianSignRole := "builder",
    target in Debian <<= (target, name in Debian, version in Debian) apply ((t,n,v) => t / (n +"-"+ v)),
    linuxPackageMappings in Debian <<= linuxPackageMappings,
    packageDescription in Debian <<= packageDescription in Linux,
    packageSummary in Debian <<= packageSummary in Linux
  ) ++ inConfig(Debian)(Seq(
    name <<= name,
    version <<= version,
    packageArchitecture := "all",
    maintainer := "",
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
        cfile
    },
    debianExplodedPackage <<= (linuxPackageMappings, debianControlFile, debianConffilesFile, target) map { (mappings, _, _, t) =>
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
      // TODO: Fix this ugly hack to permission directories correctly!
      for(file <- (t.***).get; if file.isDirectory) chmod(file, "0755")
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
          fixedName = if(name startsWith "/") name drop 1 else name
        } yield Hashing.md5Sum(file) + "  " + fixedName
        IO.writeLines(md5file, md5sums)
        md5file
    },
    packageBin <<= (debianExplodedPackage, debianMD5sumsFile, target, streams) map { (pkgdir, _, tdir, s) =>
       // Make the phackage.  We put this in fakeroot, so we can build the package with root owning files.
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
    }
  ))
  
}
