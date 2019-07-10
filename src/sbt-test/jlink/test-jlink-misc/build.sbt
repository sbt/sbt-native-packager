// Various JlinkPlugin test cases that don't warrant setting up separate
// `scripted` tests.

import scala.sys.process.Process
import com.typesafe.sbt.packager.Compat._

val runChecks = taskKey[Unit]("Run checks for a specific issue")

// Exclude Scala by default to simplify the test.
autoScalaLibrary in ThisBuild := false

// Should succeed for multi-release artifacts
val issue1243 = project
  .enablePlugins(JlinkPlugin)
  .settings(
    libraryDependencies ++= List(
      // An arbitrary multi-release artifact
      "org.apache.logging.log4j" % "log4j-core" % "2.12.0"
    ),
    // Don't bother with providing dependencies.
    jlinkIgnoreMissingDependency := JlinkIgnore.everything,
    runChecks := jlinkBuildImage.value
  )
