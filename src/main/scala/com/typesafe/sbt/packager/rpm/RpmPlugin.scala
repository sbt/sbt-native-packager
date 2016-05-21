package com.typesafe.sbt.packager.rpm

import sbt._
import sbt.Keys.{ name, version, sourceDirectory, target, packageBin, streams }
import java.nio.charset.Charset

import com.typesafe.sbt.SbtNativePackager.Linux
import com.typesafe.sbt.packager.SettingsHelper
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.linux._

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
    val Rpm = config("rpm") extend Linux
    val RpmConstants = Names
  }

  import autoImport._

  private final def osPostInstallMacro: java.net.URL = getClass getResource "brpJavaRepackJar"

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

  override def projectConfigurations: Seq[Configuration] =  Seq(Rpm)

  override lazy val projectSettings = Seq(
    rpmOs := "Linux", // TODO - default to something else?
    rpmRelease := "1",
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

    rpmScriptsDirectory <<= sourceDirectory apply (_ / "rpm" / Names.Scriptlets),
    // Explicitly defer  default settings to generic Linux Settings.
    maintainerScripts in Rpm <<= maintainerScripts in Linux,
    packageSummary in Rpm <<= packageSummary in Linux,
    packageDescription in Rpm <<= packageDescription in Linux,
    target in Rpm <<= target(_ / "rpm"),
    name in Rpm <<= name in Linux,
    packageName in Rpm <<= packageName in Linux,
    executableScriptName in Rpm <<= executableScriptName in Linux,
    rpmDaemonLogFile := s"${(packageName in Linux).value}.log",
    daemonStdoutLogFile in Rpm := Some((rpmDaemonLogFile).value),
    // override the linux sourceDirectory setting
    sourceDirectory in Rpm <<= sourceDirectory
  ) ++ inConfig(Rpm)(Seq(
      packageArchitecture := "noarch",
      rpmMetadata <<=
        (packageName, version, rpmRelease, rpmPrefix, packageArchitecture, rpmVendor, rpmOs, packageSummary, packageDescription, rpmAutoprov, rpmAutoreq) apply RpmMetadata,
      rpmDescription <<=
        (rpmLicense, rpmDistribution, rpmUrl, rpmGroup, rpmPackager, rpmIcon, rpmChangelogFile) apply RpmDescription,
      rpmDependencies <<=
        (rpmProvides, rpmRequirements, rpmPrerequisites, rpmObsoletes, rpmConflicts) apply RpmDependencies,
      maintainerScripts := {
        val scripts = maintainerScripts.value
        if (rpmBrpJavaRepackJars.value) {
          val pre = scripts.getOrElse(Names.Pre, Nil)
                val scriptBits = IO.readStream(RpmPlugin.osPostInstallMacro.openStream, Charset forName "UTF-8")
          scripts + (Names.Pre -> (pre :+ scriptBits))
        } else {
          scripts
        }
      },
      rpmScripts := RpmScripts.fromMaintainerScripts(maintainerScripts.value, linuxScriptReplacements.value),
      rpmSpecConfig <<=
        (rpmMetadata, rpmDescription, rpmDependencies, rpmSetarch, rpmScripts, linuxPackageMappings, linuxPackageSymlinks, defaultLinuxInstallLocation) map RpmSpec,
      packageBin <<= (rpmSpecConfig, target, streams) map { (spec, dir, s) =>
        spec.validate(s.log)
        RpmHelper.buildRpm(spec, dir, s.log)
      },
      rpmLint <<= (packageBin, streams) map { (rpm, s) =>
        (Process(Seq("rpmlint", "-v", rpm.getAbsolutePath)) ! s.log) match {
          case 0 => ()
          case x => sys.error("Failed to run rpmlint, exit status: " + x)
        }
      }
    ))
}

object RpmDeployPlugin extends AutoPlugin {

  import RpmPlugin.autoImport._

  override def requires = RpmPlugin

  override def projectSettings =
    SettingsHelper.makeDeploymentSettings(Rpm, packageBin in Rpm, "rpm")
}
