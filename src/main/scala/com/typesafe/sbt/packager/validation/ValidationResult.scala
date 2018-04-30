package com.typesafe.sbt.packager.validation

sealed trait ValidationResult {

  /**
    * Human readable and understandable description of the validation result.
    */
  val description: String

  /**
    * Help text on how to fix the issue.
    */
  val howToFix: String

}

final case class ValidationError(description: String, howToFix: String) extends ValidationResult
final case class ValidationWarning(description: String, howToFix: String) extends ValidationResult
