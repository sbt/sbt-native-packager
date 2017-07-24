package com.typesafe.sbt.packager

import sbt._
import sbt.Keys._
import sbt.librarymanagement.{IvyFileConfiguration, PublishConfiguration}
import com.typesafe.sbt.packager.Compat._

/**
  * TODO write tests for the SettingsHelper
  * TODO document methods properly
  * TODO document the sbt internal stuff that is used
  */
object SettingsHelper {

  def addPackage(config: Configuration,
		 packageTask: TaskKey[File],
		 extension: String,
		 classifier: Option[String] = None): Seq[Setting[_]] = inConfig(config)(
    addArtifact(
      name apply (Artifact(
	_,
	extension,
	extension,
	classifier = classifier,
	configurations = Vector.empty,
	url = None
      )),
      packageTask
    )
  )

  def makeDeploymentSettings(config: Configuration,
			     packageTask: TaskKey[File],
			     extension: String,
			     classifier: Option[String] = None): Seq[Setting[_]] =
    // Why do we need the ivyPublishSettings and jvmPublishSettings ?
    inConfig(config)(Classpaths.ivyPublishSettings ++ Classpaths.jvmPublishSettings) ++ inConfig(config)(
      Seq(
	artifacts := Seq.empty,
	packagedArtifacts := Map.empty,
	projectID := ModuleID(organization.value, name.value, version.value),
	// Why do we need a custom ModuleConfiguration for a package type?
	moduleSettings := IvyFileConfiguration(
	  validate = true,
	  scalaModuleInfo = scalaModuleInfo.value,
	  file = packageTask.value,
	  autoScalaTools = false
	),
	// Why do we have change the ivyModule?
	ivyModule := {
	  val ivy = ivySbt.value
	  new ivy.Module(moduleSettings.value)
	},
	// Where have these settings gone?
	// -------------------------------
	// deliverLocalConfiguration := Classpaths.deliverConfig(crossTarget.value, logging = ivyLoggingLevel.value)
	// deliverConfiguration := deliverLocalConfiguration.value,
	// -------------------------------
	publishConfiguration := PublishConfiguration()
	  .withResolverName(Classpaths.getPublishTo(publishTo.value).name)
	  .withArtifacts(packagedArtifacts.value.toVector)
	  .withChecksums(checksums.value.toVector)
	  .withOverwrite(isSnapshot.value)
	  .withLogging(UpdateLogging.DownloadOnly),
	publishLocalConfiguration := PublishConfiguration()
	  .withResolverName("local")
	  .withArtifacts(packagedArtifacts.value.toVector)
	  .withChecksums(checksums.value.toVector)
	  .withOverwrite(isSnapshot.value)
	  .withLogging(UpdateLogging.DownloadOnly)
      )
    ) ++ addPackage(config, packageTask, extension, classifier)

}
