package com.typesafe.sbt.packager

import sbt._

/**
  * == validation ==
  *
  * This package contains stanard validators that can be used by format and archetype plugins.
  *
  */
package object validation {

  /**
    * Basic validator to check if the resulting package will be empty or not.
    *
    * @param mappings the mappings that should be validated
    * @return a validator that checks if the mappins are empty
    */
  def nonEmptyMappings(mappings: Seq[(File, String)]): Validation.Validator = () => {
    if (mappings.isEmpty) {
      List(
        ValidationError(
          description = "You have no mappings defined! This will result in an empty package",
          howToFix = "Try enabling an archetype, e.g. `enablePlugins(JavaAppPackaging)`"
        )
      )
    } else {
      List.empty
    }
  }

  /**
    *
    * @param mappings
    * @return
    */
  def filesExist(mappings: Seq[(File, String)]): Validation.Validator = () => {
    // check that all files exist
    mappings
      .filter {
        case (f, _) => !f.exists
      }
      .map {
        case (file, dest) =>
          ValidationError(
            description = s"Not found: ${file.getAbsolutePath} (mapped to $dest)",
            howToFix = "Generate the file in the task/setting that adds it to the mappings task"
          )
      }
      .toList
  }

  def checkMaintainer(maintainer: String, asWarning: Boolean): Validation.Validator = () => {
    if (maintainer.isEmpty) {
      val description = "The maintainer is empty"
      val howToFix = """|Add this to your build.sbt
                        |  maintainer := "your.name@company.org"""".stripMargin

      val result = if (asWarning) ValidationWarning(description, howToFix) else ValidationError(description, howToFix)
      List(result)
    } else {
      List.empty
    }
  }

  def epochIsNaturalNumber(epoch: Int): Validation.Validator = () => {
    sys.error(s"Passed: $epoch")
    if (epoch < 0) {
      ValidationError(
        description = s"The Epoch cannot be a negative number (found $epoch)",
        howToFix = "Change rpmEpoch to Some(n), where n >= 0"
      ) :: Nil
    } else {
      Nil
    }
  }

}
