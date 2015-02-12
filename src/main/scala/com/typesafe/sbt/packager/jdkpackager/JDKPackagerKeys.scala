package com.typesafe.sbt.packager.jdkpackager

import sbt._

/**
 * Keys specific to deployment via the `javapackger` too.
 */
trait JDKPackagerKeys {
  val jdkPackagerTool = SettingKey[Option[File]]("jdkPackagerTool",
    "Path to `javapackager` or `javafxpackager` tool in JDK")
  val packagerArgMap = TaskKey[Map[String, String]]("packagerArgMap",
    "Command line argument key/value pairs used to generate call to `javapackager -createjar`")
  val jdkPackagerOutputBasename = SettingKey[String]("jdkPackagerOutputBasename",
    "Filename sans extension for generated installer package.")
}
