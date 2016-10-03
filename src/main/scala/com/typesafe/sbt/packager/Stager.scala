package com.typesafe.sbt.packager

import sbt._
import sbt.Keys.TaskStreams
import java.io.File

object Stager {

  /**
    * create a cache and sync files if needed
    *
    * @param config - create a configuration specific cache directory
    * @param cacheDirectory - e.g. streams.value.cacheDirectory
    * @param stageDirectory - staging directory
    * @param mappings - staging content
    *
    * @example {{{
    *
    * }}}
    */
  def stageFiles(config: String)(cacheDirectory: File,
                                 stageDirectory: File,
                                 mappings: Seq[(File, String)]): File = {
    val cache = cacheDirectory / ("packager-mappings-" + config)
    val copies = mappings map {
      case (file, path) => file -> (stageDirectory / path)
    }
    Sync(cache, FileInfo.hash, FileInfo.exists)(copies)
    // Now set scripts to executable using Java's lack of understanding of permissions.
    // TODO - Config file user-readable permissions....
    for {
      (from, to) <- copies
      if from.canExecute
    } to.setExecutable(true)
    stageDirectory
  }

  /**
    * @see stageFiles
    */
  def stage(config: String)(streams: TaskStreams,
                            stageDirectory: File,
                            mappings: Seq[(File, String)]): File = {
    stageFiles(config)(streams.cacheDirectory, stageDirectory, mappings)
  }

}
