package com.typesafe.sbt
package packager
package graalvmnativeimage

import sbt._

/**
  * GraalVM settings
  */
trait GraalVMNativeImageKeys {
  val graalVMNativeImageOptions =
    settingKey[Seq[String]]("GraalVM native-image options")

  val graalVMNativeImageGraalVersion = settingKey[Option[String]](
    "Version of GraalVM to build with. Setting this has the effect of generating a container build image to build the native image with this version of GraalVM."
  )

  val graalVMNativeImagePlatformArch = settingKey[Option[String]](
    "Platform architecture of GraalVM to build with. This only works when building the native image with a container."
  )
}

trait GraalVMNativeImageKeysEx extends GraalVMNativeImageKeys {
  val graalVMNativeImageCommand = settingKey[String]("GraalVM native-image executable command")
}
