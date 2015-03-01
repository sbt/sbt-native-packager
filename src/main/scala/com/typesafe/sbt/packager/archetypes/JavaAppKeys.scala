package com.typesafe.sbt.packager.archetypes

import sbt._

/**
 * Available settings/tasks for the [[com.typesafe.sbt.packager.archetypes.JavaAppPackaging]]
 * and all depending archetypes.
 */
trait JavaAppKeys {

  val makeBashScript = TaskKey[Option[File]]("makeBashScript", "Creates or discovers the bash script used by this project.")
  val bashScriptDefines = TaskKey[Seq[String]]("bashScriptDefines", "A list of definitions that should be written to the bash file template.")
  val bashScriptExtraDefines = TaskKey[Seq[String]]("bashScriptExtraDefines", "A list of extra definitions that should be written to the bash file template.")
  val bashScriptConfigLocation = TaskKey[Option[String]]("bashScriptConfigLocation", "The location where the bash script will load default argument configuration from.")
  val bashScriptEnvConfigLocation = SettingKey[Option[String]]("bashScriptEnvConfigLocation", "The location of a bash script that will be sourced before running the app.")
  val batScriptExtraDefines = TaskKey[Seq[String]]("batScriptExtraDefines", "A list of extra definitions that should be written to the bat file template.")
  val scriptClasspathOrdering = TaskKey[Seq[(File, String)]]("scriptClasspathOrdering", "The order of the classpath used at runtime for the bat/bash scripts.")
  val projectDependencyArtifacts = TaskKey[Seq[Attributed[File]]]("projectDependencyArtifacts", "The set of exported artifacts from our dependent projects.")
  val scriptClasspath = TaskKey[Seq[String]]("scriptClasspath", "A list of relative filenames (to the lib/ folder in the distribution) of what to include on the classpath.")
  val makeBatScript = TaskKey[Option[File]]("makeBatScript", "Creates or discovers the bat script used by this project.")
  val batScriptReplacements = TaskKey[Seq[(String, String)]](
    "batScriptReplacements",
    """|Replacements of template parameters used in the windows bat script.
         |  Default supported templates:
         |  APP_ENV_NAME - the name of the application for defining <name>_HOME variables
         |  APP_NAME - the name of the app
         |  APP_DEFINES - the defines to go into the app
         |  """.stripMargin
  )
}
