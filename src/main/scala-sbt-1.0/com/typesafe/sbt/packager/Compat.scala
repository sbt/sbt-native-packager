package com.typesafe.sbt.packager

import sbt.{PathFinder, librarymanagement => lm}
import sbt.internal.{librarymanagement => ilm, BuildDependencies => InternalBuildDependencies}
import sbt.util.CacheStore

object Compat {
  val IvyActions = ilm.IvyActions
  type IvySbt = ilm.IvySbt
  type IvyScala = sbt.librarymanagement.ScalaModuleInfo
  val IvyScala = sbt.librarymanagement.ScalaModuleInfo

  type UpdateConfiguration = lm.UpdateConfiguration

  /**
    * Used in
    * - [[com.typesafe.sbt.packager.archetypes.JavaAppPackaging]]
    */
  type BuildDependencies = InternalBuildDependencies

  /**
    */
  type Process = sys.process.Process

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
