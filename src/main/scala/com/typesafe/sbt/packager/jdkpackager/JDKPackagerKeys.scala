package com.typesafe.sbt.packager.jdkpackager

import sbt._

/**
 * Keys specific to deployment via the `javapackger` too.
 */
trait JDKPackagerKeys {
  // Setting for path to Oracle `jdkpackager` tool, which is `javafxpackager` in JDK 7
  lazy val jdkPackagerTool = SettingKey[Option[File]]("jdk-packager-tool",
    "Path to `javapackager` or `javafxpackager` tool in JDK")
}
