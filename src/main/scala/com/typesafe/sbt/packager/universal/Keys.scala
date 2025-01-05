package com.typesafe.sbt
package packager
package universal

import sbt._

trait UniversalKeys {
  val packageZipTarball =
    taskKey[PluginCompat.FileRef]("Creates a tgz package.")
  val packageXzTarball =
    taskKey[PluginCompat.FileRef]("Creates a txz package.")
  val packageOsxDmg =
    taskKey[PluginCompat.FileRef]("Creates a dmg package for macOS (only on macOS).")
  val stage = TaskKey[File](
    "stage",
    "Create a local directory with all the files laid out as they would be in the final distribution."
  )
  val dist = taskKey[PluginCompat.FileRef]("Creates the distribution packages.")
  val stagingDirectory = SettingKey[File]("stagingDirectory", "Directory where we stage distributions/releases.")
  val topLevelDirectory = SettingKey[Option[String]]("topLevelDirectory", "Top level dir in compressed output file.")
  val universalArchiveOptions =
    SettingKey[Seq[String]]("universal-archive-options", "Options passed to the tar/zip command. Scope by task")

  val containerBuildImage = taskKey[Option[String]](
    "For plugins that support building artifacts inside a docker container, if this is defined, this image will be used to do the building."
  )
}
