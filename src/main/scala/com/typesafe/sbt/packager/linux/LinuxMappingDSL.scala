package com.typesafe.sbt.packager.linux

import sbt._

trait LinuxMappingDSL {

  /** DSL for packaging files into .deb */
  def packageMapping(files: (File, String)*) = LinuxPackageMapping(files)

  /**
   * @param dir - use some directory, e.g. target.value
   * @param files
   */
  def packageTemplateMapping(files: String*)(dir: File = new File(sys.props("java.io.tmpdir"))) = LinuxPackageMapping(files map ((dir, _)))

  // TODO can the packager.MappingsHelper be used here?
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
}

object Mapper extends LinuxMappingDSL