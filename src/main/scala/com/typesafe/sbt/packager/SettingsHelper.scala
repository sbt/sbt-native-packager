package com.typesafe.sbt.packager

import sbt.{*, given}
import sbt.Keys.*
import sbt.librarymanagement.{IvyFileConfiguration, PublishConfiguration}
import com.typesafe.sbt.packager.Compat.*
import xsbti.FileConverter

/**
  *   - TODO write tests for the SettingsHelper
  *   - TODO document methods properly
  *   - TODO document the sbt internal stuff that is used
  */
object SettingsHelper {

  def addPackage(
    config: Configuration,
    packageTask: TaskKey[PluginCompat.FileRef],
    extension: String,
    classifier: Option[String] = None
  ): Seq[Setting[_]] =
    inConfig(config)(
      addArtifact(
        name.apply(Artifact(_, extension, extension, classifier = classifier, configurations = Vector.empty, None)),
        packageTask
      )
    )

  def makeDeploymentSettings(
    config: Configuration,
    packageTask: TaskKey[PluginCompat.FileRef],
    extension: String,
    classifier: Option[String] = None
  ): Seq[Setting[_]] =
    // Why do we need the ivyPublishSettings and jvmPublishSettings ?
    inConfig(config)(Classpaths.ivyPublishSettings ++ Classpaths.jvmPublishSettings) ++ inConfig(config)(
      Seq(
        artifacts := Seq.empty,
        packagedArtifacts := Map.empty,
        projectID := ModuleID(organization.value, name.value, version.value),
        // Custom module settings to skip the ivy XmlModuleDescriptorParser
        moduleSettings := ModuleDescriptorConfiguration(projectID.value, projectInfo.value)
          .withScalaModuleInfo(scalaModuleInfo.value),
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
          .withArtifacts(packagedArtifacts.value.toVector.map { case (a, f) =>
            val conv0 = fileConverter.value
            implicit val conv: FileConverter = conv0
            (a, PluginCompat.toFile(f))
          })
          .withChecksums(checksums.value.toVector)
          .withOverwrite(isSnapshot.value)
          .withLogging(UpdateLogging.DownloadOnly),
        publishLocalConfiguration := PublishConfiguration()
          .withResolverName("local")
          .withArtifacts(packagedArtifacts.value.toVector.map { case (a, f) =>
            val conv0 = fileConverter.value
            implicit val conv: FileConverter = conv0
            (a, PluginCompat.toFile(f))
          })
          .withChecksums(checksums.value.toVector)
          .withOverwrite(isSnapshot.value)
          .withLogging(UpdateLogging.DownloadOnly),
        publishM2Configuration := PublishConfiguration()
          .withResolverName(Resolver.mavenLocal.name)
          .withArtifacts(packagedArtifacts.value.toVector.map { case (a, f) =>
            val conv0 = fileConverter.value
            implicit val conv: FileConverter = conv0
            (a, PluginCompat.toFile(f))
          })
          .withChecksums(checksums.value.toVector)
          .withOverwrite(isSnapshot.value)
          .withLogging(UpdateLogging.DownloadOnly)
      )
    ) ++ addPackage(config, packageTask, extension, classifier) ++ addResolver(config)

  /**
    * SBT looks in the `otherResolvers` setting for resolvers defined in `publishTo`. If a user scopes a `publishTo`,
    * e.g.
    *
    * {{{
    * // publish the rpm to the target folder
    * publishTo in Rpm := Some(Resolver.file("target-resolver", target.value / "rpm-repo" ))
    * }}}
    *
    * then the resolver must also be present in the `otherResolvers`
    *
    * @param config
    *   the ivy configuration to look for resolvers
    */
  private def addResolver(config: Configuration): Seq[Setting[_]] =
    Seq(otherResolvers ++= (config / publishTo).value.toSeq)
}
