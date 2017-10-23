package com.typesafe.sbt.packager.archetypes.scripts

import java.io.File

import sbt._

trait ApplicationIniGenerator {
  /**
    * @return the existing mappings plus a generated application.ini
    *         if custom javaOptions are specified
    */
  def generateApplicationIni(universalMappings: Seq[(File, String)],
                             javaOptions: Seq[String],
                             bashScriptConfigLocation: Option[String],
                             tmpDir: File,
                             log: Logger): Seq[(File, String)] =
    bashScriptConfigLocation
      .collect {
        case location if javaOptions.nonEmpty =>
          val configFile = tmpDir / "tmp" / "conf" / "application.ini"
          val pathMapping = cleanApplicationIniPath(location)
          //Do not use writeLines here because of issue #637
          IO.write(configFile, ("# options from build" +: javaOptions).mkString("\n"))
          val hasConflict = universalMappings.exists {
            case (file, path) => file != configFile && path == pathMapping
          }
          // Warn the user if he tries to specify options
          if (hasConflict) {
            log.warn("--------!!! JVM Options are defined twice !!!-----------")
            log.warn(
              "application.ini is already present in output package. Will be overridden by 'javaOptions in Universal'"
            )
          }
          (configFile -> pathMapping) +: universalMappings

      }
      .getOrElse(universalMappings)

  /**
    * @param path that could be relative to app_home
    * @return path relative to app_home
    */
  protected def cleanApplicationIniPath(path: String): String
}
