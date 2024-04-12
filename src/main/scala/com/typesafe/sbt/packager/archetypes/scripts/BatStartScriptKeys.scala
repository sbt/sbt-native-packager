package com.typesafe.sbt.packager.archetypes.scripts

import sbt._

/**
  * Keys related to the [[BatStartScriptPlugin]]
  *
  * @see
  *   [[BatStartScriptPlugin]]
  */
trait BatStartScriptKeys {
  val makeBatScripts = TaskKey[Seq[(File, String)]]("makeBatScripts", "Creates start scripts for this project.")
  val batScriptTemplateLocation =
    TaskKey[File]("batScriptTemplateLocation", "The location of the bat script template.")
  val batScriptReplacements = TaskKey[Seq[(String, String)]](
    "batScriptReplacements",
    """|Replacements of template parameters used in the windows bat script.
      |  Default supported templates:
      |  APP_ENV_NAME - the name of the application for defining <name>_HOME variables
      |  APP_NAME - the name of the app
      |  APP_DEFINES - the defines to go into the app
      |  """.stripMargin
  )
  val batScriptExtraDefines = TaskKey[Seq[String]](
    "batScriptExtraDefines",
    "A list of extra definitions that should be written to the bat file template."
  )

  val batScriptConfigLocation = TaskKey[Option[String]](
    "batScriptConfigLocation",
    "The location where the bat script will load default argument configuration from."
  )
}
