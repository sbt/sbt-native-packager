package com.typesafe.sbt
package packager

import Keys._
import sbt._
import sbt.Keys.{name, mappings, sourceDirectory}
import linux.LinuxSymlink
import linux.LinuxPackageMapping

object GenericPackageSettings {
  val installLocation = "/usr/share"
}
trait GenericPackageSettings 
    extends linux.LinuxPlugin 
    with debian.DebianPlugin 
    with rpm.RpmPlugin
    with windows.WindowsPlugin
    with universal.UniversalPlugin {
  import GenericPackageSettings._
  
  // This method wires a lot of hand-coded generalities about how to map directories
  // into linux, and the conventions we expect.
  // It is by no means 100% accurate, but should be ok for the simplest cases.
  // For advanced users, use the underlying APIs.
  def mapGenericMappingsToLinux(mappings: Seq[(File, String)])(rename: String => String): Seq[LinuxPackageMapping] = {
    val (directories, nondirectories) = mappings.partition(_._1.isDirectory)
    val (binaries, nonbinaries) = nondirectories.partition(_._1.canExecute)
    val (manPages, nonManPages) = nonbinaries partition {
      case (file, name) => (name contains "man/") && (name endsWith ".1")
    }
    val compressedManPages =
      for((file, name) <- manPages)
      yield file -> (name + ".gz")
    val (configFiles, remaining) = nonManPages partition {
      case (file, name) => (name contains "etc/") || (name contains "conf/")
    }
    def packageMappingWithRename(mappings: (File, String)*): LinuxPackageMapping = {
      val renamed =
        for((file, name) <- mappings)
        yield file -> rename(name)
      packageMapping(renamed:_*)
    }
    
    Seq(
      packageMappingWithRename((binaries ++ directories):_*) withUser "root" withGroup "root" withPerms "0755",
      packageMappingWithRename(compressedManPages:_*).gzipped withUser "root" withGroup "root" withPerms "0644",
      packageMappingWithRename(configFiles:_*) withConfig("auto") withUser "root" withGroup "root" withPerms "0644",
      packageMappingWithRename(remaining:_*) withUser "root" withGroup "root" withPerms "0644"
    )  
  }
  
  def mapGenericFilesToLinux: Seq[Setting[_]] = Seq(
    // First we look at the src/linux files
    linuxPackageMappings <++= (name in Universal, sourceDirectory in Linux) map { (pkg, dir) =>
      mapGenericMappingsToLinux((dir.*** --- dir) x relativeTo(dir))(identity)
    },
    // Now we look at the src/universal files.
    linuxPackageMappings <++= (name in Universal, mappings in Universal) map { (pkg, mappings) =>
      // TODO - More windows filters...
      def isWindowsFile(f: (File, String)): Boolean =
        f._2 endsWith ".bat"
      
      mapGenericMappingsToLinux(mappings filterNot isWindowsFile) { name =>
        installLocation + "/" + pkg + "/" + name
      }
    },
    // Now we generate symlinks.
    linuxPackageSymlinks <++= (name in Universal, mappings in Universal) map { (pkg, mappings) =>
        for {
          (file, name) <- mappings
          if !file.isDirectory
          if name startsWith "bin/"
          if !(name endsWith ".bat")  // IGNORE windows-y things.
        } yield LinuxSymlink("/usr/" + name, installLocation+"/"+pkg+"/"+name)
    },
    // Map configuration files
    linuxPackageSymlinks <++= (name in Universal, mappings in Universal) map { (pkg, mappings) =>
      val needsConfLink =
        mappings exists { case (file, name) =>
          (name startsWith "conf/") && !file.isDirectory
        }
      if(needsConfLink)  Seq(LinuxSymlink(
          link="/etc/" + pkg, 
          destination=installLocation+"/"+pkg+"/conf"))
      else Seq.empty
    }
    // TODO - Map man pages?
  )

}