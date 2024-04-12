package com.typesafe.sbt
package packager

import sbt._

/**
  * General purpose keys for the native packager
  */
trait NativePackagerKeys {

  val packageName = SettingKey[String]("packageName", "Name of the created output package. Used for dirs/scripts.")
  val packageSummary = SettingKey[String]("package-summary", "Summary of the contents of a linux package.")
  val packageDescription =
    SettingKey[String]("package-description", "The description of the package.  Used when searching.")
  val maintainer = SettingKey[String]("maintainer", "The name/email address of a maintainer for the native package.")

  val executableScriptName =
    SettingKey[String]("executableScriptName", "Name of the executing script.")

  val maintainerScripts = TaskKey[Map[String, Seq[String]]]("maintainerScripts", "Scriptname to content lines")

}

/**
  * This Keys object can be used for <ul> <li>non autoplugin builds</li> <li>import single keys, which are not inside
  * the autoImport</li> </ul>
  *
  * ==Non autoplugin builds==
  *
  * {{{
  *  import com.typesafe.sbt.packager.Keys._
  *
  *  packageName := ""
  * }}}
  *
  * ==autoplugin builds==
  *
  * {{{
  *  NativePackagerKeys.packageName := ""
  * }}}
  */
object Keys
    extends NativePackagerKeys
    with universal.UniversalKeys
    with linux.LinuxKeys
    with windows.WindowsKeys
    with docker.DockerKeys
    with debian.DebianKeys
    with rpm.RpmKeys
    with archetypes.JavaAppKeys
    with archetypes.JavaAppKeys2
    with archetypes.JavaServerAppKeys
    with archetypes.jlink.JlinkKeys
    with archetypes.systemloader.SystemloaderKeys
    with archetypes.scripts.BashStartScriptKeys
    with archetypes.scripts.BatStartScriptKeys
    with validation.ValidationKeys
