package com.typesafe.sbt
package packager
package linux

import sbt._
import LinuxPlugin.Users

case class LinuxFileMetaData(
    user: String = Users.Root,
    group: String = Users.Root,
    permissions: String = "755",
    config: String = "false",
    docs: Boolean = false
) {

  def withUser(u: String) = copy(user = u)
  def withGroup(g: String) = copy(group = g)
  def withPerms(p: String) = copy(permissions = p)
  def withConfig(value: String = "true") = copy(config = value)
  def asDocs() = copy(docs = true)
}

case class LinuxPackageMapping(
    mappings: Traversable[(File, String)],
    fileData: LinuxFileMetaData = LinuxFileMetaData(),
    zipped: Boolean = false
) {

  def withUser(user: String) = copy(fileData = fileData withUser user)
  def withGroup(group: String) = copy(fileData = fileData withGroup group)
  def withPerms(perms: String) = copy(fileData = fileData withPerms perms)
  def withConfig(c: String = "true") = copy(fileData = fileData withConfig c)
  def withContents() =
    copy(mappings = Mapper.mapDirectoryAndContents(mappings.toSeq: _*))
  def asDocs() = copy(fileData = fileData asDocs ())

  /** Modifies the current package mapping to have gzipped data. */
  def gzipped = copy(zipped = true)
}

// TODO - Maybe this can support globbing symlinks?
// Maybe it should share an ancestor with LinuxPackageMapping so we can configure symlinks the same time as normal files?
case class LinuxSymlink(link: String, destination: String)
object LinuxSymlink {

  def makeRelative(from: String, to: String): String = {
    val partsFrom: Seq[String] = from split "/" filterNot (_.isEmpty)
    val partsTo: Seq[String] = to split "/" filterNot (_.isEmpty)

    val prefixAndOne = (1 to partsFrom.length)
        .map(partsFrom.take)
        .dropWhile(seq => partsTo.startsWith(seq))
        .headOption getOrElse sys.error("Cannot symlink to yourself!")
    val prefix = prefixAndOne dropRight 1
    if (prefix.length > 0) {
      val escapeCount = (partsTo.length - 1) - prefix.length
      val escapes = (0 until escapeCount) map (i => "..")
      val remainder = partsFrom drop prefix.length
      (escapes ++ remainder).mkString("/")
    } else from
  }
  // TODO - Does this belong here?
  def makeSymLinks(symlinks: Seq[LinuxSymlink],
                   pkgDir: File,
                   relativeLinks: Boolean = true): Unit = {
    for (link <- symlinks) {
      // TODO - drop preceeding '/'
      def dropFirstSlash(n: String): String =
        if (n startsWith "/") n drop 1
        else n
      def addFirstSlash(n: String): String =
        if (n startsWith "/") n
        else "/" + n
      val to = pkgDir / dropFirstSlash(link.link)
      val linkDir = to.getParentFile
      if (!linkDir.isDirectory) IO.createDirectory(linkDir)
      val name = IO.relativize(linkDir, to).getOrElse {
        sys.error(
          "Could not relativize names (" + to + ") (" + linkDir + ")!!! *(logic error)*")
      }
      val linkFinal =
        if (relativeLinks) makeRelative(link.destination, link.link)
        else addFirstSlash(link.destination)
      // from ln man page
      // -f --force remove existing destination files
      if (!to.exists)
        Process(Seq("ln", "-sf", linkFinal, name), linkDir).! match {
          case 0 => ()
          case n =>
            sys.error("Failed to symlink " + link.destination + " to " + to)
        }
    }
  }
}
