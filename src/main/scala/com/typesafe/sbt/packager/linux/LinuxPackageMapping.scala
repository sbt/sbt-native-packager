package com.typesafe.sbt
package packager
package linux

import sbt._

case class LinuxFileMetaData(
  user: String = "root",
  group: String = "root",
  permissions: String = "755",
  config: String = "false",
  docs: Boolean = false) {
  
  def withUser(u: String) = copy(user = u)
  def withGroup(g: String) = copy(group = g)
  def withPerms(p: String) = copy(permissions = p)
  def withConfig(value:String = "true") = copy(config = value)
  def asDocs() = copy(docs = true)
}

case class LinuxPackageMapping(
  mappings: Traversable[(File, String)],
  fileData: LinuxFileMetaData = LinuxFileMetaData(),
  zipped: Boolean = false) {
  
  def withUser(user: String) = copy(fileData = fileData withUser user)
  def withGroup(group: String) = copy(fileData = fileData withGroup group)
  def withPerms(perms: String) = copy(fileData = fileData withPerms perms)
  def withConfig(c: String = "true") = copy(fileData = fileData withConfig c)
  def asDocs() = copy(fileData = fileData asDocs ())
  
  /** Modifies the current package mapping to have gzipped data. */ 
  def gzipped = copy(zipped = true)
}

// TODO - Maybe this can support globbing symlinks?
// Maybe it should share an ancestor with LinuxPackageMapping so we can configure symlinks the same time as normal files?
case class LinuxSymlink(link: String, destination: String)
object LinuxSymlink {
  // TODO - Does this belong here?
  def makeSymLinks(symlinks: Seq[LinuxSymlink], pkgDir: File): Unit = {
        for(link <- symlinks) {
        // TODO - drop preceeding '/'
        def dropFirstSlash(n: String): String =
          if(n startsWith "/") n drop 1
          else n
        val from = pkgDir / dropFirstSlash(link.destination)
        val to = pkgDir / dropFirstSlash(link.link)
        val linkDir = to.getParentFile
        if(!linkDir.isDirectory) IO.createDirectory(linkDir)
        val name = IO.relativize(linkDir, to).getOrElse {
          sys.error("Could not relativize names ("+to+") ("+linkDir+")!!! *(logic error)*")
        }
        val relativeLink = 
        // TODO - if it already exists, delete it, or check accuracy...
        if(!to.exists) Process(Seq("ln", "-s", from.getAbsolutePath, name), linkDir).! match {
          case 0 => ()
          case n => sys.error("Failed to symlink " + from + " to " + to)
        }
      }
  }
}
