package com.typesafe.packager.linux

import sbt._

case class LinuxFileMetaData(
  user: String = "root",
  group: String = "root",
  permissions: String = "755") {
  
  def withUser(u: String) = copy(user = u)
  def withGroup(g: String) = copy(group = g)
  def withPerms(p: String) = copy(permissions = p)
}

case class LinuxPackageMapping(
  mappings: Traversable[(File, String)],
  fileData: LinuxFileMetaData = LinuxFileMetaData(),
  zipped: Boolean = false) {
  
  def withUser(user: String) = copy(fileData = fileData withUser user)
  def withGroup(group: String) = copy(fileData = fileData withGroup group)
  def withPerms(perms: String) = copy(fileData = fileData withPerms perms)
  
  /** Modifies the current package mapping to have gzipped data. */ 
  def gzipped = copy(zipped = true)
}