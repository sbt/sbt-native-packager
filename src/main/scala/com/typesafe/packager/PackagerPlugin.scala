package com.typesafe.packager

import sbt._

object PackagerPlugin extends Plugin 
    with linux.LinuxPlugin 
    with debian.DebianPlugin 
    with rpm.RpmPlugin {

  def packagerSettings = linuxSettings ++ debianSettings ++ rpmSettings
  
  // TODO - Add a few targets that detect the current OS and build a package for that OS.
}