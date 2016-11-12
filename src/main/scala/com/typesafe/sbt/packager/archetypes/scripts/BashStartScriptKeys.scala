package com.typesafe.sbt.packager.archetypes.scripts

import sbt._

/**
  * Keys related to the [[BashStartScriptPlugin]]
  *
  * @see [[BashStartScriptPlugin]]
  */
trait BashStartScriptKeys {
  val makeBashScripts = TaskKey[Seq[(File, String)]]("makeBashScripts", "Creates start scripts for this project.")
  val bashScriptTemplateLocation =
    TaskKey[File]("bashScriptTemplateLocation", "The location of the bash script template.")
  val bashScriptDefines = TaskKey[Seq[String]](
    "bashScriptDefines",
    "A list of definitions that should be written to the bash file template."
  )
  val bashScriptExtraDefines = TaskKey[Seq[String]](
    "bashScriptExtraDefines",
    "A list of extra definitions that should be written to the bash file template."
  )
  val bashScriptConfigLocation = TaskKey[Option[String]](
    "bashScriptConfigLocation",
    "The location where the bash script will load default argument configuration from."
  )
}
