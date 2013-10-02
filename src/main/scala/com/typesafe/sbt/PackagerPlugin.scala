package com.typesafe.sbt

import packager._

import Keys.packageMsi
import Keys.packageZipTarball
import Keys.packageXzTarball
import sbt._
import sbt.Keys.packageBin

object SbtNativePackager extends Plugin 
    with linux.LinuxPlugin 
    with debian.DebianPlugin 
    with rpm.RpmPlugin
    with windows.WindowsPlugin
    with universal.UniversalPlugin
    with GenericPackageSettings {

  val NativePackagerKeys = packager.Keys
  
  def packagerSettings = linuxSettings ++ 
                         debianSettings ++ 
                         rpmSettings ++ 
                         windowsSettings ++
                         universalSettings ++
                         Seq(                      // Bad defaults that let us at least not explode users who don't care about native packagers
                            NativePackagerKeys.maintainer := "",
                            NativePackagerKeys.packageDescription := "",
                            NativePackagerKeys.packageSummary := ""
                         )
                         
  
                         
  import SettingsHelper._
  def deploymentSettings = makeDeploymentSettings(Debian, packageBin in Debian, "deb") ++
                           makeDeploymentSettings(Rpm, packageBin in Rpm, "rpm") ++
                           makeDeploymentSettings(Windows, packageBin in Windows, "msi") ++
                           makeDeploymentSettings(Universal, packageBin in Universal, "zip") ++
                           addPackage(Universal, packageZipTarball in Universal, "tgz") ++
                           makeDeploymentSettings(UniversalDocs, packageBin in UniversalDocs, "zip") ++
                           addPackage(UniversalDocs, packageXzTarball in UniversalDocs, "txz")
  
  object packageArchetype {
    private[this] def genericMappingSettings: Seq[Setting[_]] = packagerSettings ++ mapGenericFilesToLinux ++ mapGenericFilesToWindows
    def java_application: Seq[Setting[_]] = 
      genericMappingSettings ++ archetypes.JavaAppPackaging.settings
    def java_server: Seq[Setting[_]] =
      genericMappingSettings ++ archetypes.JavaServerAppPackaging.settings
  }
                           
  // TODO - Add a few targets that detect the current OS and build a package for that OS.
  
}