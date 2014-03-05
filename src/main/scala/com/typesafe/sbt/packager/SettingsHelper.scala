package com.typesafe.sbt
package packager

import sbt._
import sbt.Keys._

object SettingsHelper {

  def addPackage(config: Configuration, packageTask: TaskKey[File], extension: String, classifier: Option[String] = None): Seq[Setting[_]] =
    inConfig(config)(addArtifact(
      name apply (Artifact(_, extension, extension, classifier = classifier, configurations = Iterable.empty, url = None, extraAttributes = Map.empty)),
      packageTask
    ))

  def makeDeploymentSettings(config: Configuration, packageTask: TaskKey[File], extension: String): Seq[Setting[_]] =
    (inConfig(config)(Classpaths.publishSettings)) ++ inConfig(config)(Seq(
      artifacts := Seq.empty,
      packagedArtifacts := Map.empty,
      projectID <<= (organization, name, version) apply { (o, n, v) => ModuleID(o, n, v) },
      moduleSettings <<= (projectID, projectInfo) map { (pid, pinfo) =>
        InlineConfiguration(pid, pinfo, Seq.empty)
      },
      ivyModule <<= (ivySbt, moduleSettings) map { (i, s) => new i.Module(s) },
      deliverLocalConfiguration <<= (crossTarget, ivyLoggingLevel) map { (outDir, level) => Classpaths.deliverConfig(outDir, logging = level) },
      deliverConfiguration <<= deliverLocalConfiguration,
      publishConfiguration <<= (packagedArtifacts, checksums, publishTo) map { (as, checks, publishTo) =>
        new PublishConfiguration(ivyFile = None,
          resolverName = Classpaths.getPublishTo(publishTo).name,
          artifacts = as,
          checksums = checks,
          logging = UpdateLogging.DownloadOnly)
      },
      publishLocalConfiguration <<= (packagedArtifacts, checksums) map { (as, checks) =>
        new PublishConfiguration(ivyFile = None,
          resolverName = "local",
          artifacts = as,
          checksums = checks,
          logging = UpdateLogging.DownloadOnly)
      })) ++ addPackage(config, packageTask, extension)
}