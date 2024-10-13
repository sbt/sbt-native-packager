// Tests jlink behavior with missing dependencies.

import scala.sys.process.Process
import com.typesafe.sbt.packager.Compat._

// Exclude Scala to simplify the test
ThisBuild / autoScalaLibrary := false

// Simulate a missing dependency (foo -> bar)
lazy val foo = project.dependsOn(bar % "provided")
lazy val bar = project

lazy val withoutIgnore = project
  .dependsOn(foo)
  .enablePlugins(JlinkPlugin)

lazy val withIgnore = project
  .dependsOn(foo)
  .enablePlugins(JlinkPlugin)
  .settings(jlinkIgnoreMissingDependency := JlinkIgnore.only("foo" -> "bar"))
