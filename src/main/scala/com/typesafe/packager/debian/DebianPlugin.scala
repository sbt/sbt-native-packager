package com.typesafe.packager
package debian

import Keys._
import sbt._
import sbt.Keys.sourceFilter
import com.typesafe.packager.linux.LinuxPackageMapping
import com.typesafe.packager.linux.LinuxFileMetaData

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
    Process("chmod " + perms + " " + file.getAbsolutePath).!
  
  def debianSettings: Seq[Setting[_]] = Seq(
    debianPriority := "optional",
    debianSection := "java",
    debianPackageDependencies := Seq.empty,
    debianPackageRecommends := Seq.empty,
    target in Debian <<= (target, name in Debian, version in Debian) apply ((t,n,v) => t / (n +"-"+ v)),
    linuxPackageMappings in Debian <<= linuxPackageMappings,
    packageDescription in Debian <<= packageDescription in Linux,
    packageSummary in Debian <<= packageSummary in Linux
  ) ++ inConfig(Debian)(Seq(
    name <<= name,
    version <<= version,
    packageArchitecture := "all",
    debianPackageInfo <<=
      (name, version, maintainer, packageSummary, packageDescription) apply PackageInfo,
    debianPackageMetadata <<= 
      (debianPackageInfo,
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
    }
  ))
  
}