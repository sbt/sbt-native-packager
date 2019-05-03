package com.typesafe.sbt.packager.archetypes

import sbt._

/**
  * See [[JavaAppKeys]]. These are private to preserve binary compatibility.
  */
// TODO: merge with JavaAppKeys when we can break binary compatibility
private[packager] trait JavaAppKeys2 {
  val bundledJvmLocation =
    TaskKey[Option[String]]("bundledJvmLocation", "The location of the bundled JVM image")
}
