package com.typesafe.sbt.packager.validation

import sbt.Logger

/**
  * Validation result.
  *
  * @param errors all errors that were found during the validation
  * @param warnings all warnings that were found during the validation
  */
final case class Validation(errors: List[ValidationError], warnings: List[ValidationWarning])

object Validation {

  /**
    * A validator is a function that returns a list of validation results.
    *
    *
    * @example Usually a validator is a function that captures some setting or task value, e.g.
    * {{{
    *   validatePackageValidators += {
    *     val universalMappings = (mappings in Universal).value
    *     () => {
    *       if (universalMappings.isEmpty) List(ValidationError(...)) else List.empt
    *     }
    *   }
    * }}}
    *
    * The `validation` package object contains various standard validators.
    *
    */
  type Validator = () => List[ValidationResult]

  /**
    *
    * @param validators a list of validators that produce a `Validation` result
    * @return aggregated result of all validator function
    */
  def apply(validators: Seq[Validator]): Validation = validators.flatMap(_.apply()).foldLeft(Validation(Nil, Nil)) {
    case (validation, error: ValidationError) => validation.copy(errors = validation.errors :+ error)
    case (validation, warning: ValidationWarning) => validation.copy(warnings = validation.warnings :+ warning)
  }

  /**
    * Runs a list of validators and throws an exception after printing all
    * errors and warnings with the provided logger.
    *
    * @param validators a list of validators that produce the validation result
    * @param log used to print errors and warnings
    */
  def runAndThrow(validators: Seq[Validator], log: Logger): Unit = {
    val Validation(errors, warnings) = apply(validators)

    warnings.zipWithIndex.foreach {
      case (warning, i) =>
        log.warn(s"[${i + 1}] ${warning.description}")
        log.warn(warning.howToFix)
    }

    errors.zipWithIndex.foreach {
      case (error, i) =>
        log.error(s"[${i + 1}] ${error.description}")
        log.error(error.howToFix)
    }

    if (errors.nonEmpty) {
      sys.error(s"${errors.length} error(s) found")
    }

    log.success("All package validations passed")
  }

}
