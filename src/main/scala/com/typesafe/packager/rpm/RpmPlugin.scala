package com.typesafe.packager
package rpm

import linux._
import sbt._

object Keys {
  // METADATA keys.
  def name = sbt.Keys.name
  def version = sbt.Keys.version
  def maintainer = linux.Keys.maintainer
  def packageArchitecture = linux.Keys.packageArchitecture
  def packageDescription = linux.Keys.packageDescription
  val rpmVendor = SettingKey[String]("rpm-vendor", "Name of the vendor for this RPM.")
  val rpmOs = SettingKey[String]("rpm-os", "Name of the os for this RPM.")
  val rpmRelease = SettingKey[String]("rpm-release", "Special release number for this rpm (vs. the software).")
  val rpmMetadata = SettingKey[RpmMetadata]("rpm-metadata", "Metadata associated with the generated RPM.")
  
  // DESCRIPTION KEYS
  val rpmSummary = SettingKey[Option[String]]("rpm-summary", "Summary of the contents of an RPM package.")
  val rpmLicense = SettingKey[Option[String]]("rpm-license", "License of the code within the RPM.")
  val rpmDistribution = SettingKey[Option[String]]("rpm-distribution")
  val rpmUrl = SettingKey[Option[String]]("rpm-url", "Url to include in the RPM.")
  val rpmGroup = SettingKey[Option[String]]("rpm-group", "Group to associate with the RPM.")
  val rpmPackager = SettingKey[Option[String]]("rpm-packger", "Person who packaged this rpm.")
  val rpmIcon = SettingKey[Option[String]]("rpm-icon", "name of the icon to use with this RPM.")
  val rpmDescription = SettingKey[RpmDescription]("rpm-description", "Description of this rpm.")
  
  // DEPENDENCIES
  val rpmProvides = SettingKey[Seq[String]]("rpm-provides", "Packages this RPM provides.")
  val rpmRequirements = SettingKey[Seq[String]]("rpm-requirements", "Packages this RPM requires.")
  val rpmPrerequisites = SettingKey[Seq[String]]("rpm-prerequisites", "Packages this RPM need *before* installation.")
  val rpmObsoletes = SettingKey[Seq[String]]("rpm-obsoletes", "Packages this RPM makes obsolete.")
  val rpmConflicts = SettingKey[Seq[String]]("rpm-conflicts", "Packages this RPM conflicts with.")
  val rpmDependencies = SettingKey[RpmDependencies]("rpm-dependencies", "Configuration of dependency info for this RPM.")
  
  // SPEC
  def linuxPackageMappings = linux.Keys.linuxPackageMappings
  val rpmSpecConfig = TaskKey[RpmSpec]("rpm-spec-config", "All the configuration for an RPM .spec file.")
  
  // Building
  def target = sbt.Keys.target
  def packageBin = sbt.Keys.packageBin
  
  def streams = sbt.Keys.streams
}

object RpmPlugin extends Plugin {
  import Keys._
  
  val Rpm = config("rpm")
  
  def rpmSettings: Seq[Setting[_]] = Seq(
    rpmSummary := None,
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
    target in Rpm <<= target(_ / "rpm")
  ) ++ inConfig(Rpm)(Seq(
    rpmMetadata <<=
      (name, version, rpmRelease, packageArchitecture, rpmVendor, rpmOs) apply (RpmMetadata.apply),
    rpmDescription <<=
      (rpmSummary, rpmLicense, rpmDistribution, rpmUrl, rpmGroup, rpmPackager, rpmIcon) apply RpmDescription,
    rpmDependencies <<=
      (rpmProvides, rpmRequirements, rpmPrerequisites, rpmObsoletes, rpmConflicts) apply RpmDependencies,
    rpmSpecConfig <<=
      (rpmMetadata, rpmDescription, rpmDependencies, linuxPackageMappings) map RpmSpec,
    packageBin <<= (rpmSpecConfig, target, streams) map { (spec, dir, s) =>
        RpmHelper.buildRpm(spec, dir, s.log)
    }
  ))
}