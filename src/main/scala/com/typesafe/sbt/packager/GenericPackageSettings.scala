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
  // Right now, it's also pretty focused on command line scripts packages.
  
  /**
   * Maps linux file format from the universal from the conventions:
   * 
   * `<project>/src/linux` files are mapped directly into linux packages.
   * `<universal>` files are placed under `/usr/share/<package-name>`
   * `<universal>/bin` files are given symlinks in `/usr/bin`
   * `<universal>/conf` directory is given a symlink to `/etc/<package-name>`
   * Files in `conf/` or `etc/` directories are automatically marked as configuration.
   * `../man/...1` files are automatically compressed into .gz files.
   */
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
      packageMappingWithRename(configFiles:_*) withConfig() withUser "root" withGroup "root" withPerms "0644",
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
  )
  
  def mapGenericFilesToWinows: Seq[Setting[_]] = Seq(
    mappings in Windows <<= mappings in Universal,
    wixFeatures <<= (name in Windows, mappings in Windows) map makeWindowsFeatures
  )
  // TODO select main script!  Filter Config links!
  def makeWindowsFeatures(name: String, mappings: Seq[(File, String)]): Seq[windows.WindowsFeature] = {
    import windows._
    
    val files =
      for {
        (file, name) <- mappings
        if !file.isDirectory
      } yield ComponentFile(name, editable = (name startsWith "conf/"))
    val corePackage =
      WindowsFeature(
        id=name+"Core",
        title=name,
        desc="All core files.",
        absent="disallow",
        components = files
      )
    // TODO - Detect bat files to add paths...
    val homeEnvVar = name.toUpperCase +"_HOME"
    val addBinToPath =
      // TODO - we may have issues here...
      WindowsFeature(
        id="AddBinToPath",
        title="Update Enviornment Variables",
        desc="Update PATH environment variables (requires restart).",
        components = Seq(AddDirectoryToPath("bin"))
      )
    val configFileLinks =
      WindowsFeature(
          id="AddConfigLinks",
          title="Configuration start menu links",
          desc="Adds start menu shortcuts to edit configuration files.",
          components = for {
             (file, name) <- mappings
             if !file.isDirectory
             if name startsWith "conf/"
          } yield AddShortCut(name)
      )
    // TODO - Add feature for shortcuts to binary scripts.
    Seq(corePackage, addBinToPath, configFileLinks)
  }
  
  
}