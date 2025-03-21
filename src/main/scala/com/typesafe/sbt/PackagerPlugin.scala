package com.typesafe.sbt

import packager.*
import debian.DebianPlugin.autoImport.genChanges
import com.typesafe.sbt.packager.Keys.{packageXzTarball, packageZipTarball, validatePackage, validatePackageValidators}
import com.typesafe.sbt.packager.validation.Validation
import sbt.{*, given}
import sbt.Keys.{name, normalizedName, packageBin, streams}

/**
  * ==SBT Native Packager Plugin==
  *
  * This is the top level plugin for the sbt native packager. You don't have to enable this by yourself, instead we
  * recommend using an archetype for this.
  *
  * Currently you can choose between
  *
  *   - JavaAppPackaging
  *   - JavaServerPackaging
  *
  * ==Configuration==
  *
  * There are a few settings you should set if you want to build package no matter what format.
  *
  * {{{
  * maintainer := "Your name <your.name@your-company.org>"
  * packageDescription := "A short description of your application"
  * }}}
  *
  * For all other general settings take a look at [[com.typesafe.sbt.packager.NativePackagerKeys]]
  *
  * @example
  *   Enable the plugin in the `build.sbt`
  *   {{{
  *    enablePlugins(SbtNativePackager)
  *   }}}
  */
object SbtNativePackager extends AutoPlugin {

  /* === Universal Configuration === */
  val Universal = universal.UniversalPlugin.autoImport.Universal
  val UniversalDocs = universal.UniversalPlugin.autoImport.UniversalDocs
  val UniversalSrc = universal.UniversalPlugin.autoImport.UniversalSrc

  /* === OS Configurations === */
  val Linux = linux.LinuxPlugin.autoImport.Linux
  val Debian = debian.DebianPlugin.autoImport.Debian
  val Rpm = rpm.RpmPlugin.autoImport.Rpm
  val Windows = windows.WindowsPlugin.autoImport.Windows
  val Docker = docker.DockerPlugin.autoImport.Docker

  /**
    * imports all [[com.typesafe.sbt.packager.NativePackagerKeys]] and two objects:
    *
    * ===NativePackagerKeys===
    *
    * This includes ''all'' available keys provided by the sbt-native-packager. Used it if a setting/task key is not in
    * scope.
    *
    * {{{
    * NativePackagerKeys.notAutomaticallyImported := "cool!"
    * }}}
    *
    * ===NativePackagerHelper===
    *
    * This object contains a set of helper methods for working with mappings.
    */
  object autoImport extends packager.NativePackagerKeys {

    val NativePackagerKeys = packager.Keys
    val NativePackagerHelper = packager.MappingsHelper
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    // Bad defaults that let us at least not explode users who don't care about native packagers
    maintainer := "",
    packageDescription := name.value,
    packageSummary := name.value,
    packageName := normalizedName.value,
    executableScriptName := normalizedName.value,
    maintainerScripts := Map(),
    // no validation by default
    validatePackageValidators := Seq.empty,
    validatePackage := Validation.runAndThrow(validatePackageValidators.value, streams.value.log)
  )

  object packageArchetype {

    /**
      * ==Recommended usage==
      *
      * {{{
      * enablePlugins(JavaAppPackaging)
      * }}}
      */
    @deprecated("Use enablePlugins(JavaAppPackaging)", "1.x")
    def java_application: Seq[Setting[?]] =
      projectSettings ++
        universal.UniversalPlugin.projectSettings ++
        linux.LinuxPlugin.projectSettings ++
        debian.DebianPlugin.projectSettings ++
        rpm.RpmPlugin.projectSettings ++
        docker.DockerPlugin.projectSettings ++
        windows.WindowsPlugin.projectSettings ++
        archetypes.JavaAppPackaging.projectSettings

    /**
      * {{{
      * enablePlugins(JavaServerAppPackaging)
      * }}}
      */
    @deprecated("Use enablePlugins(JavaServerAppPackaging)", "1.x")
    def java_server: Seq[Setting[?]] =
      java_application ++ archetypes.JavaServerAppPackaging.projectSettings
  }

  // TODO - Add a few targets that detect the current OS and build a package for that OS.

}
