package com.typesafe.sbt.packager.archetypes.scripts

import sbt.{File, SettingKey, TaskKey}

/**
  * Keys related to the [[AshStartScriptPlugin]]
  *
  * @see [[AshStartScriptPlugin]]
  */
trait AshStartScriptKeys {

  val makeAshScripts = TaskKey[Seq[(File, String)]]("makeAshScripts", "Creates start script/scripts for this project.")
  val ashScriptTemplateName = SettingKey[String]("ashScriptTemplateName", "The name of the ash script template")
  val ashScriptTemplateLocation = TaskKey[File]("ashScriptTemplateLocation", "The location of the ash script template.")
  val ashScriptReplacements = TaskKey[Seq[(String, String)]](
    "ashScriptReplacements",
    "Replacements of template parameters used in the ash script."
  )

}
