package com.typesafe.sbt.packager

import sbt._

/** A set of helper methods to simplify the writing of mappings */
object MappingsHelper {

  /**
    * return a Seq of mappings which effect is to add a whole directory in the generated package
    *
    * @example
    * {{{
    * mappings in Universal ++= directory(baseDirectory.value / "extra")
    * }}}
    *
    * @param sourceDir
    * @return mappings
    */
  def directory(sourceDir: File): Seq[(File, String)] =
    Option(sourceDir.getParentFile)
      .map(parent => sourceDir.*** pair relativeTo(parent))
      .getOrElse(sourceDir.*** pair basic)

  /**
    * It lightens the build file if one wants to give a string instead of file.
    *
    * @example
    * {{{
    * mappings in Universal ++= directory("extra")
    * }}}
    *
    * @param sourceDir
    * @return mappings
    */
  def directory(sourceDir: String): Seq[(File, String)] =
    directory(file(sourceDir))

  /**
    * return a Seq of mappings which effect is to add the content of directory in the generated package,
    * excluding the directory itself.
    *
    * @example
    * {{{
    * mappings in Universal ++= sourceDir(baseDirectory.value / "extra")
    * }}}
    *
    * @param sourceDir
    * @return mappings
    */
  def contentOf(sourceDir: File): Seq[(File, String)] =
    (sourceDir.*** --- sourceDir) pair relativeTo(sourceDir)

  /**
    * It lightens the build file if one wants to give a string instead of file.
    *
    * @example
    * {{{
    * mappings in Universal ++= sourceDir("extra")
    * }}}
    *
    * @param sourceDir as string representation
    * @return mappings
    */
  def contentOf(sourceDir: String): Seq[(File, String)] =
    contentOf(file(sourceDir))

  /**
    * Create mappings from your classpath. For example if you want to add additional
    * dependencies, like test or model.
    *
    * @example Add all test artifacts to a separated test folder
    * {{{
    * mappings in Universal ++= fromClasspath((managedClasspath in Test).value, target = "test")
    * }}}
    *
    * @param entries
    * @param target
    * @return a list of mappings
    */
  def fromClasspath(entries: Seq[Attributed[File]], target: String): Seq[(File, String)] =
    fromClasspath(entries, target, _ => true)

  /**
    * Create mappings from your classpath. For example if you want to add additional
    * dependencies, like test or model. You can also filter the artifacts that should
    * be mapped to mappings.
    *
    * @example Filter all osgi bundles
    * {{{
    * mappings in Universal ++= fromClasspath(
    *    (managedClasspath in Runtime).value,
    *    "osgi",
    *    artifact => artifact.`type` == "bundle"
    * )
    * }}}
    *
    * @param entries from where mappings should be created from
    * @param target folder, e.g. `model`. Must not end with a slash
    * @param includeArtifact function to determine if an artifact should result in a mapping
    * @param includeOnNoArtifact default is false. When there's no Artifact meta data remove it
    */
  def fromClasspath(
    entries: Seq[Attributed[File]],
    target: String,
    includeArtifact: Artifact => Boolean,
    includeOnNoArtifact: Boolean = false
  ): Seq[(File, String)] =
    entries.filter(attr => attr.get(sbt.Keys.artifact.key) map includeArtifact getOrElse includeOnNoArtifact).map {
      attribute =>
        val file = attribute.data
        file -> s"$target/${file.getName}"
    }

  /**
    * Get the mappings for the given files relative to the given directories.
    */
  def relative(files: Seq[File], dirs: Seq[File]): Seq[(File, String)] =
    (files --- dirs) pair (relativeTo(dirs) | Path.flat)

}
