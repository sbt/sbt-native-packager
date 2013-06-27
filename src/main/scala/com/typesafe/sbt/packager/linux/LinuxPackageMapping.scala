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
case class LinuxSymlink(link: String, destination: String)
