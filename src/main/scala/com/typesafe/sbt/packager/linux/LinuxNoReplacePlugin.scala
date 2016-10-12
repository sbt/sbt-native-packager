package com.typesafe.sbt.packager.linux

import com.typesafe.sbt.packager.rpm.RpmPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin.autoImport
import sbt._

/**
  * Created by carsten on 12.10.16.
  */
object LinuxNoReplacePlugin extends AutoPlugin with LinuxKeys {
  override def requires = RpmPlugin

  override def projectSettings = inConfig(autoImport.Rpm)(Seq(
    linuxPackageMappings := configWithNoReplace(linuxPackageMappings.value),
    makeEtcDefault := makeEtcDefault.value
  ))

  def configWithNoReplace(mappings: Seq[LinuxPackageMapping]): Seq[LinuxPackageMapping] = {
    mappings.map {
      case mapping if mapping.fileData.config != "false" => mapping.withConfig("noreplace")
      case mapping => mapping
    }
  }
}
