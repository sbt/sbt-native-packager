package com.typesafe.sbt.packager.archetypes.scripts

import sbt.{*, given}
import com.typesafe.sbt.packager.PluginCompat

/**
  * Keys related to the [[BashStartScriptPlugin]]
  *
  * @see
  *   [[BashStartScriptPlugin]]
  */
trait BashStartScriptKeys {
  @transient
  val makeBashScripts = taskKey[Seq[(PluginCompat.FileRef, String)]]("Creates start scripts for this project.")
  @transient
  val bashScriptTemplateLocation =
    TaskKey[File]("bashScriptTemplateLocation", "The location of the bash script template.")

  @transient
  val bashScriptReplacements = TaskKey[Seq[(String, String)]](
    "bashScriptReplacements",
    """|Replacements of template parameters used in the bash script.
       |  Default supported templates:
       |  app_mainclass - the main class that should be executed
       |""".stripMargin
  )

  @transient
  val bashScriptDefines =
    TaskKey[Seq[String]]("bashScriptDefines", "A list of definitions that should be written to the bash file template.")
  @transient
  val bashScriptExtraDefines = TaskKey[Seq[String]](
    "bashScriptExtraDefines",
    "A list of extra definitions that should be written to the bash file template."
  )
  @transient
  val bashScriptConfigLocation = TaskKey[Option[String]](
    "bashScriptConfigLocation",
    "The location where the bash script will load default argument configuration from."
  )

  @transient
  val bashForwarderTemplateLocation =
    TaskKey[Option[File]]("bashForwarderTemplateLocation", "The location of the bash forwarder template")
}
