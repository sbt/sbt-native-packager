package com.typesafe.sbt.packager.rpm

import sbt._
import sbt.Keys._
import java.nio.charset.Charset

import com.typesafe.sbt.SbtNativePackager.Linux
import com.typesafe.sbt.packager.SettingsHelper
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.linux._
import com.typesafe.sbt.packager.Compat._
import com.typesafe.sbt.packager.validation._

/**
  * Plugin containing all generic values used for packaging rpms.
  *
  * @example Enable the plugin in the `build.sbt`
  * {{{
  *    enablePlugins(RpmPlugin)
  * }}}
  */
object RpmPlugin extends AutoPlugin {

  override def requires = LinuxPlugin

  object autoImport extends RpmKeys {
    val Rpm: Configuration = config("rpm") extend Linux
    val RpmConstants = Names
  }

  import autoImport._

  private final def osPostInstallMacro: java.net.URL =
    getClass getResource "brpJavaRepackJar"

  /** RPM specific names */
  object Names {
    val Scriptlets = "scriptlets"

    //maintainer script names
    /** `pretrans` */
    val Pretrans = "pretrans"

    /** `postinst` */
    val Post = "post"

    /** `preinst` */
    val Pre = "pre"

    /** `postun` */
    val Postun = "postun"

    /** `preun` */
    val Preun = "preun"

    /** `verifyscript` */
    val Verifyscript = "verifyscript"

    /** `posttrans` */
    val Posttrans = "posttrans"

  }

  override def projectConfigurations: Seq[Configuration] = Seq(Rpm)

  override lazy val projectSettings = Seq(
    rpmOs := "Linux", // TODO - default to something else?
    rpmRelease := (if (isSnapshot.value) "SNAPSHOT" else "1"),
    rpmPrefix := None,
    rpmVendor := "", // TODO - Maybe pull in organization?
    rpmLicense := None,
    rpmDistribution := None,
    rpmUrl := None,
    rpmGroup := None,
    rpmPackager := None,
    rpmIcon := None,
    rpmAutoprov := "yes",
    rpmAutoreq := "yes",
    rpmProvides := Seq.empty,
    rpmRequirements := Seq.empty,
    rpmPrerequisites := Seq.empty,
    rpmObsoletes := Seq.empty,
    rpmConflicts := Seq.empty,
    rpmSetarch := None,
    rpmChangelogFile := None,
    rpmBrpJavaRepackJars := false,
    rpmPretrans := None,
    rpmPre := None,
    rpmPost := None,
    rpmVerifyscript := None,
    rpmPosttrans := None,
    rpmPreun := None,
    rpmPostun := None,
    rpmScriptsDirectory := sourceDirectory.value / "rpm" / Names.Scriptlets,
    // Explicitly defer  default settings to generic Linux Settings.
    maintainerScripts in Rpm := (maintainerScripts in Linux).value,
    packageSummary in Rpm := (packageSummary in Linux).value,
    packageDescription in Rpm := (packageDescription in Linux).value,
    target in Rpm := target.value / "rpm",
    name in Rpm := (name in Linux).value,
    packageName in Rpm := (packageName in Linux).value,
    executableScriptName in Rpm := (executableScriptName in Linux).value,
    rpmDaemonLogFile := s"${(packageName in Linux).value}.log",
    daemonStdoutLogFile in Rpm := Some(rpmDaemonLogFile.value),
    validatePackageValidators in Rpm := Seq(
      nonEmptyMappings((mappings in Rpm).value),
      filesExist((mappings in Rpm).value),
      checkMaintainer((maintainer in Rpm).value, asWarning = false)
    ),
    // override the linux sourceDirectory setting
    sourceDirectory in Rpm := sourceDirectory.value,
    packageArchitecture in Rpm := "noarch",
    rpmMetadata := RpmMetadata(
      (packageName in Rpm).value,
      (version in Rpm).value.stripSuffix("-SNAPSHOT"),
      rpmRelease.value,
      rpmPrefix.value,
      (packageArchitecture in Rpm).value,
      rpmVendor.value,
      rpmOs.value,
      (packageSummary in Rpm).value,
      (packageDescription in Rpm).value,
      rpmAutoprov.value,
      rpmAutoreq.value
    ),
    rpmDescription := RpmDescription(
      rpmLicense.value,
      rpmDistribution.value,
      rpmUrl.value,
      rpmGroup.value,
      rpmPackager.value,
      rpmIcon.value,
      rpmChangelogFile.value
    ),
    rpmDependencies := RpmDependencies(
      rpmProvides.value,
      rpmRequirements.value,
      rpmPrerequisites.value,
      rpmObsoletes.value,
      rpmConflicts.value
    ),
    maintainerScripts in Rpm := {
      val scripts = (maintainerScripts in Rpm).value
      if (!rpmBrpJavaRepackJars.value) {
        val pre = scripts.getOrElse(Names.Pre, Nil)
        val scriptBits = IO.readStream(RpmPlugin.osPostInstallMacro.openStream, Charset forName "UTF-8")
        scripts + (Names.Pre -> (pre :+ scriptBits))
      } else {
        scripts
      }
    },
    rpmScripts := RpmScripts
      .fromMaintainerScripts((maintainerScripts in Rpm).value, (linuxScriptReplacements in Rpm).value),
    rpmSpecConfig := RpmSpec(
      rpmMetadata.value,
      rpmDescription.value,
      rpmDependencies.value,
      rpmSetarch.value,
      rpmScripts.value,
      (linuxPackageMappings in Rpm).value,
      (linuxPackageSymlinks in Rpm).value,
      (defaultLinuxInstallLocation in Rpm).value
    ),
    stage in Rpm := RpmHelper.stage(rpmSpecConfig.value, (target in Rpm).value, streams.value.log),
    packageBin in Rpm := RpmHelper.buildRpm(rpmSpecConfig.value, (stage in Rpm).value, streams.value.log),
    rpmLint := {
      sys.process.Process(Seq("rpmlint", "-v", (packageBin in Rpm).value.getAbsolutePath)) ! streams.value.log match {
        case 0 => ()
        case x => sys.error("Failed to run rpmlint, exit status: " + x)
      }
    }
  )
}

object RpmDeployPlugin extends AutoPlugin {

  import RpmPlugin.autoImport._

  override def requires = RpmPlugin

  override def projectSettings: Seq[Setting[_]] =
    SettingsHelper.makeDeploymentSettings(Rpm, packageBin in Rpm, "rpm")
}
