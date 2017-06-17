package com.typesafe.sbt.packager

import sbt.{PathFinder, librarymanagement => lm}
import sbt.internal.{librarymanagement => ilm, BuildDependencies => InternalBuildDependencies}
import sbt.util.CacheStore

object Compat {
  val IvyActions = ilm.IvyActions
  type PublishConfiguration = ilm.PublishConfiguration
  type IvySbt = ilm.IvySbt
  type IvyScala = lm.IvyScala
  type UpdateConfiguration = lm.UpdateConfiguration
  type InlineConfiguration = ilm.InlineConfiguration
  val InlineConfiguration = ilm.InlineConfiguration

  /**
    * Used in
    * - [[com.typesafe.sbt.packager.archetypes.JavaAppPackaging]]
    */
  type BuildDependencies = InternalBuildDependencies

  /**
    * Used in
    * - [[com.typesafe.sbt.packager.docker.DockerPlugin]]
    */
  type ProcessLogger = sys.process.ProcessLogger

  /**
    *  Used in
    * - [[com.typesafe.sbt.packager.Stager]]
    * @param file
    * @return a CacheStore
    */
  implicit def fileToCacheStore(file: java.io.File): CacheStore = CacheStore(file)
}
