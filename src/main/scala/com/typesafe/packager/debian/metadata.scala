package com.typesafe.packager.debian



/** Represents package meta used by debian when constructing packages. */
case class PackageMetaData(
  name: String,
  version: String,
  maintainer: String,
  description: String,
  priority: String = "optional",
  architecture: String = "all",
  section: String = "java",
  depends: Seq[String] = Seq.empty,
  recommends: Seq[String] = Seq.empty
) {
  def makeContent: String = {
    // TODO: Pretty print with line wrap.
    val sb = new StringBuilder
    sb append ("Package:      %s\n" format name)
    sb append ("Version:      %s\n" format version)
    sb append ("Section:      %s\n" format section)
    sb append ("Priority:     %s\n" format priority)
    sb append ("Architecture: %s\n" format architecture)
    if(!depends.isEmpty)
      sb append ("Depends:      %s\n" format (depends mkString ", "))
    if(!recommends.isEmpty)
      sb append ("Recommends:   %s\n" format (recommends mkString ", "))
    sb append ("Maintainer:   %s\n" format maintainer)
    sb append ("Description: %s\n\n" format description)
    sb toString
  }
}