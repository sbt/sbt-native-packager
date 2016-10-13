package com.typesafe.sbt.packager.linux

import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
import com.typesafe.sbt.packager.rpm.RpmPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin.autoImport
import sbt._

/**
  * This plugin automatically updates all config files in a RPM to "noreplace". Those files will not be overwritten
  * during an update if they have been changed on disk. This is useful for server apps.
  */
object RpmNoReplaceConfigPlugin extends AutoPlugin with LinuxKeys with LinuxMappingDSL {
  override def requires = JavaServerAppPackaging && RpmPlugin

  override def projectSettings = inConfig(autoImport.Rpm)(Seq(
    linuxPackageMappings := configWithNoReplace(linuxPackageMappings.value)
  ))
}
