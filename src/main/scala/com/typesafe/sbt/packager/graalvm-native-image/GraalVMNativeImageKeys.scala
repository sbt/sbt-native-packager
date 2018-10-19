package com.typesafe.sbt
package packager
package graalvmnativeimage

import sbt._

/**
  * GraalVM settings
  */
trait GraalVMNativeImageKeys {
  val graalVMNativeImageOptions =
    SettingKey[Seq[String]]("graalvm-native-image-options", "GraalVM native-image options")
}
