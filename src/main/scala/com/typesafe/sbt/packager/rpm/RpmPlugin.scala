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
  * @example
  *   Enable the plugin in the `build.sbt`
  *   {{{
  *    enablePlugins(RpmPlugin)
  *   }}}
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

    // maintainer script names
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
    rpmEpoch := None,
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
    Rpm / maintainerScripts := (Linux / maintainerScripts).value,
    Rpm / packageSummary := (Linux / packageSummary).value,
    Rpm / packageDescription := (Linux / packageDescription).value,
    Rpm / target := target.value / "rpm",
    Rpm / name := (Linux / name).value,
    Rpm / packageName := (Linux / packageName).value,
    Rpm / executableScriptName := (Linux / executableScriptName).value,
    rpmDaemonLogFile := s"${(Linux / packageName).value}.log",
    Rpm / daemonStdoutLogFile := Some(rpmDaemonLogFile.value),
    Rpm / validatePackageValidators := Seq(
      nonEmptyMappings((Rpm / linuxPackageMappings).value.flatMap(_.mappings)),
      filesExist((Rpm / linuxPackageMappings).value.flatMap(_.mappings)),
      checkMaintainer((Rpm / maintainer).value, asWarning = false),
      epochIsNaturalNumber((Rpm / rpmEpoch).value.getOrElse(0))
    ),
    // override the linux sourceDirectory setting
    Rpm / sourceDirectory := sourceDirectory.value,
    Rpm / packageArchitecture := "noarch",
    rpmMetadata := RpmMetadata(
      (Rpm / packageName).value,
      (Rpm / version).value.stripSuffix("-SNAPSHOT"),
      rpmRelease.value,
      rpmPrefix.value,
      (Rpm / packageArchitecture).value,
      rpmVendor.value,
      rpmOs.value,
      (Rpm / packageSummary).value,
      (Rpm / packageDescription).value,
      rpmAutoprov.value,
      rpmAutoreq.value,
      rpmEpoch.value
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
    Rpm / maintainerScripts := {
      val scripts = (Rpm / maintainerScripts).value
      if (!rpmBrpJavaRepackJars.value) {
        val pre = scripts.getOrElse(Names.Pre, Nil)
        val scriptBits = IO.readStream(RpmPlugin.osPostInstallMacro.openStream, Charset forName "UTF-8")
        scripts + (Names.Pre -> (pre :+ scriptBits))
      } else
        scripts
    },
    rpmScripts := RpmScripts
      .fromMaintainerScripts((Rpm / maintainerScripts).value, (Rpm / linuxScriptReplacements).value),
    rpmSpecConfig := RpmSpec(
      rpmMetadata.value,
      rpmDescription.value,
      rpmDependencies.value,
      rpmSetarch.value,
      rpmScripts.value,
      (Rpm / linuxPackageMappings).value,
      (Rpm / linuxPackageSymlinks).value,
      (Rpm / defaultLinuxInstallLocation).value
    ),
    Rpm / stage := RpmHelper.stage(rpmSpecConfig.value, (Rpm / target).value, streams.value.log),
    Rpm / packageBin / artifactPath := RpmHelper.defaultRpmArtifactPath((Rpm / target).value, rpmMetadata.value),
    Rpm / packageBin := {
      val defaultPath = RpmHelper.buildRpm(rpmSpecConfig.value, (Rpm / stage).value, streams.value.log)
      // `file` points to where buildRpm created the rpm. However we want it to be at `artifactPath`.
      // If `artifactPath` is not the default value then we need to copy the file.
      val path = (Rpm / packageBin / artifactPath).value
      if (path.getCanonicalFile != defaultPath.getCanonicalFile) IO.copyFile(defaultPath, path)
      path
    },
    rpmLint := {
      sys.process.Process(Seq("rpmlint", "-v", (Rpm / packageBin).value.getAbsolutePath)) ! streams.value.log match {
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
    SettingsHelper.makeDeploymentSettings(Rpm, Rpm / packageBin, "rpm")
}
