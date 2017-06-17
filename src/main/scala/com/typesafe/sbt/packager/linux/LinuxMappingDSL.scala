package com.typesafe.sbt.packager.linux

import sbt._

trait LinuxMappingDSL {

  /** DSL for packaging files into .deb */
  def packageMapping(files: (File, String)*) = LinuxPackageMapping(files)

  /**
    * @param dir - use some directory, e.g. target.value
    * @param files
    */
  def packageTemplateMapping(files: String*)(dir: File = new File(sys.props("java.io.tmpdir"))) =
    LinuxPackageMapping(files map ((dir, _)))

  // TODO can the packager.MappingsHelper be used here?
  /**
    * @see #mapDirectoryAndContents
    * @param dirs - directories to map
    */
  def packageDirectoryAndContentsMapping(dirs: (File, String)*) =
    LinuxPackageMapping(mapDirectoryAndContents(dirs: _*))

  /**
    * This method includes files and directories.
    *
    * @param dirs - directories to map
    */
  def mapDirectoryAndContents(dirs: (File, String)*): Seq[(File, String)] =
    for {
      (src, dest) <- dirs
      path <- (src ** AllPassFilter).get
    } yield path -> path.toString.replaceFirst(src.toString, dest)

  /**
    * This method sets the config attribute of all files that are marked as configuration files to "noreplace". This is
    * relevant for RPM packages as it controls the behaviour of RPM updates.
    *
    * See: http://www-uxsup.csx.cam.ac.uk/~jw35/docs/rpm_config.html
    *
    * @param mappings list of mappings to update
    * @return updated list of mappings
    */
  def configWithNoReplace(mappings: Seq[LinuxPackageMapping]): Seq[LinuxPackageMapping] =
    mappings.map {
      case mapping if mapping.fileData.config != "false" => mapping.withConfig("noreplace")
      case mapping                                       => mapping
    }
}

object Mapper extends LinuxMappingDSL
