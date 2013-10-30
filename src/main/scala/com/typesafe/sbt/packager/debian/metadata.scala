package com.typesafe.sbt
package packager
package debian

case class PackageInfo(
  name: String,
  version: String,
  maintainer: String,
  summary: String,
  description: String)

/** Represents package meta used by debian when constructing packages. */
case class PackageMetaData(
  info: PackageInfo,
  priority: String = "optional",
  architecture: String = "all",
  section: String = "java",
  depends: Seq[String] = Seq.empty,
  recommends: Seq[String] = Seq.empty) {
  def makeContent(installSizeEstimate: Long = 0L): String = {
    // TODO: Pretty print with line wrap.
    val sb = new StringBuilder
    sb append ("Package: %s\n" format info.name)
    sb append ("Version: %s\n" format info.version)
    sb append ("Section: %s\n" format section)
    sb append ("Priority: %s\n" format priority)
    sb append ("Architecture: %s\n" format architecture)
    sb append ("Installed-Size: %d\n" format installSizeEstimate)
    if (!depends.isEmpty)
      sb append ("Depends: %s\n" format (depends mkString ", "))
    if (!recommends.isEmpty)
      sb append ("Recommends: %s\n" format (recommends mkString ", "))
    sb append ("Maintainer: %s\n" format info.maintainer)
    sb append ("Description: %s\n %s\n" format (info.summary, info.description))
    sb toString
  }
}

/**
 * This replacements are use for the debian maintainer scripts:
 * preinst, postinst, prerm, postrm
 */
case class DebianControlScriptReplacements(
  author: String,
  descr: String,
  name: String,
  version: String) {

  /**
   * Generates the replacement sequence for the debian
   * maintainer scripts
   */
  def makeReplacements(): Seq[(String, String)] = Seq(
    "author" -> author,
    "descr" -> descr,
    "name" -> name,
    "version" -> version)
}