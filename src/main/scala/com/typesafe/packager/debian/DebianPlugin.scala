package com.typesafe.packager
package debian

import Keys._
import sbt._
import sbt.Keys.sourceFilter
import com.typesafe.packager.linux.LinuxPackageMapping
import com.typesafe.packager.linux.LinuxFileMetaData

object DebianPlugin extends Plugin {
  val Debian = config("debian")
  /** DSL for packaging files into .deb */
  def packageForDebian(files: (File, String)*) = LinuxPackageMapping(files)
  
  private[this] final def copyAndFixPerms(from: File, to: File, perms: LinuxFileMetaData, zipped: Boolean = false): Unit = {
    if(zipped) IO.gzip(from, to)
    else IO.copyFile(from, to, true)
    // If we have a directory, we need to alter the perms.
    chmod(to, perms.permissions)
    // TODO - Can we do anything about user/group ownership?
  }
  
  private[this] final def chmod(file: File, perms: String): Unit =
    Process("chmod " + perms + " " + file.getAbsolutePath).!
  
  final def makeMan(file: File): String = 
    Process("groff -man -Tascii " + file.getAbsolutePath).!!
  
  def debianSettings: Seq[Setting[_]] = Seq(
    // TODO - These settings should move to a common 'linux packaging' plugin location.
    linuxPackageMappings := Seq.empty,
    packageArchitecture := "all",
    sourceDiectory in Debian <<= sourceDiectory apply (_ / "linux"),
    debianPriority := "optional",
    debianSection := "java",
    debianPackageDependencies := Seq.empty,
    debianPackageRecommends := Seq.empty,
    target in Debian <<= (target, name in Debian, version in Debian) apply ((t,n,v) => t / (n +"-"+ v)),
    linuxPackageMappings in Debian <<= (linuxPackageMappings).identity
  ) ++ inConfig(Debian)(Seq(
    name <<= name,
    version <<= version,
    packageDescription := "",
    debianPackageMetadata <<= 
      (name, version, maintainer, packageDescription, 
       debianPriority, packageArchitecture, debianSection, 
       debianPackageDependencies, debianPackageRecommends) apply PackageMetaData,
    debianControlFile <<= (debianPackageMetadata, target) map {
      (data, dir) =>
        val cfile = dir / "DEBIAN" / "control"
        IO.write(cfile, data.makeContent, java.nio.charset.Charset.defaultCharset)
        cfile
    },
    debianExplodedPackage <<= (linuxPackageMappings, debianControlFile, target) map { (mappings, _, t) =>
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
    packageBin <<= (debianExplodedPackage, target, name, version) map { (pkgdir, tdir, n, v) =>
       // Make the phackage.  We put this in fakeroot, so we can build the package with root owning files.
       Process(Seq("fakeroot", "--", "dpkg-deb", "--build", pkgdir.getAbsolutePath), Some(tdir)).!
      tdir.getParentFile / (n + "-" + v + ".deb")
    },
    lintian <<= packageBin map { file =>
      Process(Seq("lintian", "-c", "-v", file.getName), Some(file.getParentFile)).!
    },
    generateManPages <<= (sourceDiectory, sbt.Keys.streams) map { (dir, s) =>
      for( file <- (dir / "usr/share/man/man1" ** "*.1").get ) {
        val man = makeMan(file)
        s.log.info("Generated man page for[" + file + "] =")
        s.log.info(man)
      }  
    }
  ))
  
}