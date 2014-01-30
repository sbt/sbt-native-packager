package com.typesafe.sbt
package packager
package linux

import Keys._
import sbt._
import com.typesafe.sbt.packager.linux.LinuxPlugin.Users

/**
 * Plugin trait containing all the generic values used for
 * packaging linux software.
 */
trait LinuxPlugin extends Plugin {
  // TODO - is this needed
  val Linux = config("linux")

  def linuxSettings: Seq[Setting[_]] = Seq(
    linuxPackageMappings := Seq.empty,
    linuxPackageSymlinks := Seq.empty,
    sourceDirectory in Linux <<= sourceDirectory apply (_ / "linux"),
    generateManPages <<= (sourceDirectory in Linux, sbt.Keys.streams) map { (dir, s) =>
      for (file <- (dir / "usr/share/man/man1" ** "*.1").get) {
        val man = makeMan(file)
        s.log.info("Generated man page for[" + file + "] =")
        s.log.info(man)
      }
    },
    packageSummary in Linux <<= packageSummary,
    packageDescription in Linux <<= packageDescription,
    appUser := Users.Root)

  /** DSL for packaging files into .deb */
  def packageMapping(files: (File, String)*) = LinuxPackageMapping(files)

  /**
   * @param dir - use some directory, e.g. target.value
   * @param files
   */
  def packageTemplateMapping(files: String*)(dir: File = new File(sys.props("java.io.tmpdir"))) = LinuxPackageMapping(files map ((dir, _)))

  /**
   * @see #mapDirectoryAndContents
   * @param dirs - directories to map
   */
  def packageDirectoryAndContentsMapping(dirs: (File, String)*) = LinuxPackageMapping(mapDirectoryAndContents(dirs: _*))

  /**
   * This method includes files and directories.
   *
   * @param dirs - directories to map
   */
  def mapDirectoryAndContents(dirs: (File, String)*): Seq[(File, String)] = for {
    (src, dest) <- dirs
    path <- (src ***).get
  } yield path -> path.toString.replaceFirst(src.toString, dest)

  // TODO - we'd like a set of conventions to take universal mappings and create linux package mappings.

  /** Create a ascii friendly string for a man page. */
  final def makeMan(file: File): String =
    Process("groff -man -Tascii " + file.getAbsolutePath).!!
}

object LinuxPlugin {
  object Users {
    val Root = "root"
  }
}