package com.typesafe.sbt.packager.validation

import sbt._

trait ValidationKeys {

  /**
    * A task that implements various validations for a format. Example usage:
    *   - `sbt Universal/packageBin/validatePackage`
    *   - `sbt Debian/packageBin/validatePackage`
    *
    * Each format should implement it's own validate. Implemented in #1026
    */
  @transient
  val validatePackage = taskKey[Unit]("validates the package configuration")

  @transient
  val validatePackageValidators = taskKey[Seq[Validation.Validator]]("validator functions")
}

object ValidationKeys extends ValidationKeys
