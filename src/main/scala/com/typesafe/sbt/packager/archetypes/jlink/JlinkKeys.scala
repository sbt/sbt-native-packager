package com.typesafe.sbt.packager.archetypes
package jlink

import sbt._

/**
  * Available settings/tasks for the [[com.typesafe.sbt.packager.archetypes.jlink.JlinkPlugin]].
  */
private[packager] trait JlinkKeys {

  val jlinkBundledJvmLocation =
    TaskKey[String]("jlinkBundledJvmLocation", "The location of the resulting JVM image")

  val jlinkModules = TaskKey[Seq[String]]("jlinkModules", "Modules to link")

  val jlinkIgnoreMissingDependency =
    TaskKey[((String, String)) => Boolean](
      "jlinkIgnoreMissingDependency",
      "A hook to mask missing package dependency issues"
    )

  val jlinkOptions =
    TaskKey[Seq[String]]("jlinkOptions", "Options for the jlink utility")

  val jlinkBuildImage =
    TaskKey[File]("jlinkBuildImage", "Runs jlink. Yields the directory with the runtime image")
}
