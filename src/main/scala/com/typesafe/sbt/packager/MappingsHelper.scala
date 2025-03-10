package com.typesafe.sbt.packager

import sbt.{*, given}
import sbt.io.*
import xsbti.FileConverter

/** A set of helper methods to simplify the writing of mappings */
object MappingsHelper extends Mapper {

  /**
    * It lightens the build file if one wants to give a string instead of file.
    *
    * @example
    *   {{{
    * mappings in Universal ++= directory("extra")
    *   }}}
    *
    * @param sourceDir
    * @return
    *   mappings
    */
  def directory(sourceDir: String): Seq[(File, String)] =
    directory(file(sourceDir))

  /**
    * It lightens the build file if one wants to give a string instead of file.
    *
    * @example
    *   {{{
    * mappings in Universal ++= contentOf("extra")
    *   }}}
    *
    * @param sourceDir
    *   as string representation
    * @return
    *   mappings
    */
  def contentOf(sourceDir: String): Seq[(File, String)] =
    contentOf(file(sourceDir))

  def contentOf(sourceDir: File, conv0: FileConverter): Seq[(PluginCompat.FileRef, String)] = {
    implicit val conv: FileConverter = conv0
    contentOf(sourceDir).map { case (f, p) =>
      PluginCompat.toFileRef(f) -> p
    }
  }

  /**
    * Create mappings from your classpath. For example if you want to add additional dependencies, like test or model.
    *
    * @example
    *   Add all test artifacts to a separated test folder
    *   {{{
    * mappings in Universal ++= fromClasspath((managedClasspath in Test).value, target = "test")
    *   }}}
    *
    * @param entries
    * @param target
    * @return
    *   a list of mappings
    */
  def fromClasspath(
    entries: Seq[Attributed[PluginCompat.FileRef]],
    target: String
  ): Seq[(PluginCompat.FileRef, String)] =
    fromClasspath(entries, target, _ => true)

  /**
    * Create mappings from your classpath. For example if you want to add additional dependencies, like test or model.
    * You can also filter the artifacts that should be mapped to mappings.
    *
    * @example
    *   Filter all osgi bundles
    *   {{{
    * mappings in Universal ++= fromClasspath(
    *    (managedClasspath in Runtime).value,
    *    "osgi",
    *    artifact => artifact.`type` == "bundle"
    * )
    *   }}}
    *
    * @param entries
    *   from where mappings should be created from
    * @param target
    *   folder, e.g. `model`. Must not end with a slash
    * @param includeArtifact
    *   function to determine if an artifact should result in a mapping
    * @param includeOnNoArtifact
    *   default is false. When there's no Artifact meta data remove it
    */
  def fromClasspath(
    entries: Seq[Attributed[PluginCompat.FileRef]],
    target: String,
    includeArtifact: PluginCompat.IncludeArtifact,
    includeOnNoArtifact: Boolean = false
  ): Seq[(PluginCompat.FileRef, String)] =
    entries.filter(attr => attr.get(PluginCompat.artifactStr).map(includeArtifact) getOrElse includeOnNoArtifact).map {
      attribute =>
        val file = attribute.data
        val name = PluginCompat.getName(file)
        file -> s"$target/${name}"
    }

  /**
    * Get the mappings for the given files relative to the given directories.
    */
  def relative(files: Seq[File], dirs: Seq[File], conv0: FileConverter): Seq[(PluginCompat.FileRef, String)] = {
    implicit val conv: FileConverter = conv0
    (files --- dirs) pair (relativeTo(dirs) | flat) map { case (f, p) =>
      PluginCompat.toFileRef(f) -> p
    }
  }
}
