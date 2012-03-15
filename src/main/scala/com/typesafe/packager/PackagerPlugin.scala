package com.typesafe.packager

import Keys.packageMsi
import sbt._
import sbt.Keys.packageBin

object PackagerPlugin extends Plugin 
    with linux.LinuxPlugin 
    with debian.DebianPlugin 
    with rpm.RpmPlugin
    with windows.WindowsPlugin
    with universal.UniversalPlugin {

  def packagerSettings = linuxSettings ++ 
                         debianSettings ++ 
                         rpmSettings ++ 
                         windowsSettings ++
                         universalSettings
  
  import SettingsHelper._
  def deploymentSettings = makeDeploymentSettings(Debian, packageBin in Debian, "deb") ++
                           makeDeploymentSettings(Rpm, packageBin in Rpm, "rpm") ++
                           makeDeploymentSettings(Windows, packageMsi in Windows, "msi")
  
  // TODO - Add a few targets that detect the current OS and build a package for that OS.
}