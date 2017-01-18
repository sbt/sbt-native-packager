package com.typesafe.sbt.packager.rpm

import sbt._
import sbt.Keys.{name, packageBin, sourceDirectory, streams, target, version}
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
    rpmBrpJavaRepackJars := true,
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
    sourceDirectory in Rpm <<= sourceDirectory,
    packageArchitecture in Rpm := "noarch",
    rpmMetadata <<=
      (packageName in Rpm,
       version in Rpm,
       rpmRelease,
       rpmPrefix,
       packageArchitecture in Rpm,
       rpmVendor,
       rpmOs,
       packageSummary in Rpm,
       packageDescription in Rpm,
       rpmAutoprov,
       rpmAutoreq) apply RpmMetadata,
    rpmDescription <<=
      (rpmLicense, rpmDistribution, rpmUrl, rpmGroup, rpmPackager, rpmIcon, rpmChangelogFile) apply RpmDescription,
    rpmDependencies <<=
      (rpmProvides, rpmRequirements, rpmPrerequisites, rpmObsoletes, rpmConflicts) apply RpmDependencies,
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
    rpmSpecConfig <<=
      (rpmMetadata,
       rpmDescription,
       rpmDependencies,
       rpmSetarch,
       rpmScripts,
       linuxPackageMappings in Rpm,
       linuxPackageSymlinks in Rpm,
       defaultLinuxInstallLocation in Rpm) map RpmSpec,
    packageBin in Rpm <<= (rpmSpecConfig, target in Rpm, streams) map { (spec, dir, s) =>
      spec.validate(s.log)
      RpmHelper.buildRpm(spec, dir, s.log)
    },
    rpmLint <<= (packageBin in Rpm, streams) map { (rpm, s) =>
      (Process(Seq("rpmlint", "-v", rpm.getAbsolutePath)) ! s.log) match {
        case 0 => ()
        case x => sys.error("Failed to run rpmlint, exit status: " + x)
      }
    }
  )
}

object RpmDeployPlugin extends AutoPlugin {

  import RpmPlugin.autoImport._

  override def requires = RpmPlugin

  override def projectSettings =
    SettingsHelper.makeDeploymentSettings(Rpm, packageBin in Rpm, "rpm")
}
