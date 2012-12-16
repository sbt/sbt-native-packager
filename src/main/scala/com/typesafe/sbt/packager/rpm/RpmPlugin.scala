package com.typesafe.sbt
package packager
package rpm

import Keys._
import linux._
import sbt._

/** Plugin trait containing all generic values used for packaging linux software. */
trait RpmPlugin extends Plugin with LinuxPlugin {
  val Rpm = config("rpm") extend Linux
  
  def rpmSettings: Seq[Setting[_]] = Seq(
    rpmOs := "Linux",  // TODO - default to something else?
    rpmRelease := "0",
    rpmVendor := "",  // TODO - Maybe pull in organization?
    rpmLicense := None,
    rpmDistribution := None,
    rpmUrl := None,
    rpmGroup := None,
    rpmPackager := None,
    rpmIcon := None,
    rpmProvides := Seq.empty,
    rpmRequirements := Seq.empty,
    rpmPrerequisites := Seq.empty,
    rpmObsoletes := Seq.empty,
    rpmConflicts := Seq.empty,
    rpmPretrans := None,
    rpmPre := None,
    rpmPost := None,
    rpmVerifyscript := None,
    rpmPosttrans := None,
    rpmPreun := None,
    rpmPostun := None,
    packageSummary in Rpm <<= packageSummary in Linux,
    packageDescription in Rpm <<= packageDescription in Linux,
    target in Rpm <<= target(_ / "rpm")
  ) ++ inConfig(Rpm)(Seq(
    packageArchitecture := "noarch",
    rpmMetadata <<=
      (name, version, rpmRelease, packageArchitecture, rpmVendor, rpmOs, packageSummary, packageDescription) apply (RpmMetadata.apply),
    rpmDescription <<=
      (rpmLicense, rpmDistribution, rpmUrl, rpmGroup, rpmPackager, rpmIcon) apply RpmDescription,
    rpmDependencies <<=
      (rpmProvides, rpmRequirements, rpmPrerequisites, rpmObsoletes, rpmConflicts) apply RpmDependencies,
    rpmScripts <<=
      (rpmPretrans,rpmPre,rpmPost,rpmVerifyscript,rpmPosttrans,rpmPreun,rpmPostun) apply RpmScripts,
    rpmSpecConfig <<=
      (rpmMetadata, rpmDescription, rpmDependencies, rpmScripts, linuxPackageMappings) map RpmSpec,
    packageBin <<= (rpmSpecConfig, target, streams) map { (spec, dir, s) =>
        RpmHelper.buildRpm(spec, dir, s.log)
    },
    rpmLint <<= (packageBin, streams) map { (rpm, s) =>
       (Process(Seq("rpmlint", "-v", rpm.getAbsolutePath)) ! s.log)  match {
          case 0 => ()
          case x => sys.error("Failed to run rpmlint, exit status: " + x)
       }
    }
  ))
}
