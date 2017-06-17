package com.typesafe.sbt.packager

import sbt.Keys._
import sbt._
import com.typesafe.sbt.packager.Compat._

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
    inConfig(config)(Classpaths.ivyPublishSettings ++ Classpaths.jvmPublishSettings) ++ inConfig(config)(
      Seq(
	artifacts := Seq.empty,
	packagedArtifacts := Map.empty,
	projectID := ModuleID(organization.value, name.value, version.value),
	// TODO find out why we have this InlineConfiguration here
	moduleSettings := InlineConfiguration(true, ivyScala.value, projectID.value, projectInfo.value, Vector.empty),
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
	  checksums = checksums.value.toVector,
	  logging = UpdateLogging.DownloadOnly,
	  overwrite = isSnapshot.value
	),
	publishLocalConfiguration := new PublishConfiguration(
	  ivyFile = None,
	  resolverName = "local",
	  artifacts = packagedArtifacts.value,
	  checksums = checksums.value.toVector,
	  logging = UpdateLogging.DownloadOnly,
	  overwrite = isSnapshot.value
	)
      )
    ) ++ addPackage(config, packageTask, extension, classifier)
}
