package com.typesafe.sbt

import packager._

import debian.DebianPlugin.autoImport.genChanges
import universal.UniversalPlugin.autoImport.{ packageZipTarball, packageXzTarball }
import sbt._
import sbt.Keys.{ name, normalizedName, packageBin }

/**
 * == SBT Native Packager Plugin ==
 *
 * This is the top level plugin for the sbt native packager.
 * You don't have to enable this by yourself, instead we recommend
 * using an archetype for this.
 *
 * Currently you can choose between
 *
 * <ul>
 * <li>JavaAppPackaging</li>
 * <li>JavaServerPackaging</li>
 * <li>AkkaAppPackging</li>
 * </ul>
 *
 * == Configuration ==
 *
 * The are a few settings you should set if you want to build package
 * no matter what format.
 *
 * {{{
 * maintainer := "Your name <your.name@your-company.org>"
 * packageDescription := "A short description of your application"
 * }}}
 *
 * For all other general settings take a look at [[com.typesafe.sbt.packager.NativePackagerKeys]]
 *
 * @example Enable the plugin in the `build.sbt`
 * {{{
 *    enablePlugins(SbtNativePackager)
 * }}}
 *
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
   * === NativePackagerKeys ===
   *
   * This inclues ''all'' available keys provided by the sbt-native-packager.
   * Used it if a setting/task key is not in scope.
   *
   * {{{
   * NativePackagerKeys.notAutomaticallyImported := "cool!"
   * }}}
   *
   * === NativePackagerHelper ===
   *
   * This object contains a set of helper methods for working with mappings.
   *
   */
  object autoImport extends packager.NativePackagerKeys {

    val NativePackagerKeys = packager.Keys
    val NativePackagerHelper = packager.MappingsHelper

    import SettingsHelper._

    def deploymentSettings = makeDeploymentSettings(Debian, packageBin in Debian, "deb") ++
      makeDeploymentSettings(Rpm, packageBin in Rpm, "rpm") ++
      makeDeploymentSettings(Windows, packageBin in Windows, "msi") ++
      makeDeploymentSettings(Universal, packageBin in Universal, "zip") ++
      addPackage(Universal, packageZipTarball in Universal, "tgz") ++
      makeDeploymentSettings(UniversalDocs, packageBin in UniversalDocs, "zip") ++
      addPackage(UniversalDocs, packageXzTarball in UniversalDocs, "txz") ++
      makeDeploymentSettings(Debian, genChanges in Debian, "changes")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    // Bad defaults that let us at least not explode users who don't care about native packagers
    maintainer := "",
    packageDescription := name.value,
    packageSummary := name.value,
    packageName := normalizedName.value,
    executableScriptName := normalizedName.value

  )

  object packageArchetype {

    /**
     * == Recommended usage ==
     *
     * {{{
     * enablePlugins(JavaAppPackaging)
     * }}}
     */
    @deprecated("Use enablePlugins(JavaAppPackaging)", "1.x")
    def java_application: Seq[Setting[_]] =
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
     * enablePlugins(AkkaAppPackaging)
     * }}}
     */
    @deprecated("Use enablePlugins(AkkaAppPackaging)", "1.x")
    def akka_application: Seq[Setting[_]] = java_application ++ archetypes.AkkaAppPackaging.projectSettings

    /**
     * {{{
     * enablePlugins(JavaServerAppPackaging)
     * }}}
     */
    @deprecated("Use enablePlugins(JavaServerAppPackaging)", "1.x")
    def java_server: Seq[Setting[_]] = java_application ++ archetypes.JavaServerAppPackaging.projectSettings
  }

  // TODO - Add a few targets that detect the current OS and build a package for that OS.

}
