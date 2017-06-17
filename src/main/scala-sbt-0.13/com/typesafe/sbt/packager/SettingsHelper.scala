package com.typesafe.sbt.packager

import sbt.Keys._
import sbt._

object SettingsHelper {

  def addPackage(config: Configuration,
		 packageTask: TaskKey[File],
		 extension: String,
		 classifier: Option[String] = None): Seq[Setting[_]] =
    inConfig(config)(
      addArtifact(
	name apply (Artifact(
	  _,
	  extension,
	  extension,
	  classifier = classifier,
	  configurations = Iterable.empty,
	  url = None,
	  extraAttributes = Map.empty
	)),
	packageTask
      )
    )

  def makeDeploymentSettings(config: Configuration,
			     packageTask: TaskKey[File],
			     extension: String,
			     classifier: Option[String] = None): Seq[Setting[_]] =
    inConfig(config)(Classpaths.ivyPublishSettings ++ Classpaths.jvmPublishSettings) ++ inConfig(config)(
      Seq(
	artifacts := Seq.empty,
	packagedArtifacts := Map.empty,
	projectID := ModuleID(organization.value, name.value, version.value),
	moduleSettings := InlineConfiguration(projectID.value, projectInfo.value, Seq.empty),
	ivyModule := {
	  val ivy = ivySbt.value
	  new ivy.Module(moduleSettings.value)
	},
	deliverLocalConfiguration := Classpaths.deliverConfig(crossTarget.value, logging = ivyLoggingLevel.value),
	deliverConfiguration := deliverLocalConfiguration.value,
	publishConfiguration := new PublishConfiguration(
	  ivyFile = None,
	  resolverName = Classpaths.getPublishTo(publishTo.value).name,
	  artifacts = packagedArtifacts.value,
	  checksums = checksums.value,
	  logging = UpdateLogging.DownloadOnly,
	  overwrite = isSnapshot.value
	),
	publishLocalConfiguration := new PublishConfiguration(
	  ivyFile = None,
	  resolverName = "local",
	  artifacts = packagedArtifacts.value,
	  checksums = checksums.value,
	  logging = UpdateLogging.DownloadOnly,
	  overwrite = isSnapshot.value
	)
      )
    ) ++ addPackage(config, packageTask, extension, classifier)
}
