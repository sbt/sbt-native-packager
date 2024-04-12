package com.typesafe.sbt.packager.validation

import sbt._

trait ValidationKeys {

  /**
    * A task that implements various validations for a format. Example usage:
    *   - `sbt universal:packageBin::validatePackage`
    *   - `sbt debian:packageBin::validatePackage`
    *
    * Each format should implement it's own validate. Implemented in #1026
    */
  val validatePackage = taskKey[Unit]("validates the package configuration")

  val validatePackageValidators = taskKey[Seq[Validation.Validator]]("validator functions")
}

object ValidationKeys extends ValidationKeys
