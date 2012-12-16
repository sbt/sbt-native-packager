package com.typesafe.sbt
package packager
package rpm

import linux._
import sbt._

/** RPM Specific keys. */
trait RpmKeys {
  // METADATA keys.
  val rpmVendor = SettingKey[String]("rpm-vendor", "Name of the vendor for this RPM.")
  val rpmOs = SettingKey[String]("rpm-os", "Name of the os for this RPM.")
  val rpmRelease = SettingKey[String]("rpm-release", "Special release number for this rpm (vs. the software).")
  val rpmMetadata = SettingKey[RpmMetadata]("rpm-metadata", "Metadata associated with the generated RPM.")
  
  // DESCRIPTION KEYS
  // TODO - Summary and license are required.
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
  val rpmSpecConfig = TaskKey[RpmSpec]("rpm-spec-config", "All the configuration for an RPM .spec file.")
  
 // SCRIPTS
  val rpmScripts = SettingKey[RpmScripts]("rpm-scripts", "Configuration of pre- and post-integration scripts.")

  val rpmPretrans = SettingKey[Option[String]]("rpm-pretrans", "%pretrans scriptlet")
  val rpmPre = SettingKey[Option[String]]("rpm-pre", "%pre scriptlet")
  val rpmVerifyscript = SettingKey[Option[String]]("rpm-verifyscipt", "%verifyscript scriptlet")
  val rpmPost = SettingKey[Option[String]]("rpm-post", "%post scriptlet")
  val rpmPosttrans = SettingKey[Option[String]]("rpm-posttrans", "%posttrans scriptlet")
  val rpmPreun = SettingKey[Option[String]]("rpm-preun", "%preun scriptlet")
  val rpmPostun = SettingKey[Option[String]]("rpm-postun", "%postun scriptlet")

  // Building
  val rpmLint = TaskKey[Unit]("rpm-lint", "Runs rpmlint program against the genreated RPM, if available.")
}

/** Keys used in RPM Settings. */
object Keys extends RpmKeys {
  // METADATA keys.
  def name = sbt.Keys.name
  def version = sbt.Keys.version
  def maintainer = linux.Keys.maintainer
  def packageArchitecture = linux.Keys.packageArchitecture
  def packageDescription = linux.Keys.packageDescription
  def packageSummary = linux.Keys.packageSummary
  
  // DESCRIPTION KEYS
  
  // DEPENDENCIES
  
  // SPEC
  def linuxPackageMappings = linux.Keys.linuxPackageMappings
  
  // Building
  def target = sbt.Keys.target
  def packageBin = sbt.Keys.packageBin
  
  def streams = sbt.Keys.streams  
}
