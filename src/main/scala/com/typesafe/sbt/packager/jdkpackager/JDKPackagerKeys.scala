package com.typesafe.sbt.packager.jdkpackager

import sbt._

/**
 * Keys specific to deployment via the `javapackger` tool.
 *
 * @author <a href="mailto:fitch@datamininglab.com">Simeon H.K. Fitch</a>
 * @since 2/11/15
 */
trait JDKPackagerKeys {
  val jdkPackagerTool = SettingKey[Option[File]]("jdkPackagerTool",
    "Path to `javapackager` or `javafxpackager` tool in JDK.")

  val packagerArgMap = TaskKey[Map[String, String]]("packagerArgMap",
    "An intermediate task for computing the command line argument key/value pairs passed to " +
      "`javapackager -deploy`")

  val jdkPackagerBasename = SettingKey[String]("jdkPackagerOutputBasename",
    "Filename sans extension for generated installer package.")

  val jdkPackagerType = SettingKey[String]("jdkPackagerType",
    """Value passed as the `-native` argument to `javapackager -deploy`.
      | Per `javapackager` documentation, this may be one of the following:
      |
      |    * `all`: Runs all of the installers for the platform on which it is running,
      |      and creates a disk image for the application.
      |    * `installer`: Runs all of the installers for the platform on which it is running.
      |    * `image`: Creates a disk image for the application. On OS X, the image is
      |      the .app file. On Linux, the image is the directory that gets installed.
      |    * `dmg`: Generates a DMG file for OS X.
      |    * `pkg`: Generates a .pkg package for OS X.
      |    * `mac.appStore`: Generates a package for the Mac App Store.
      |    * `rpm`: Generates an RPM package for Linux.
      |    * `deb`: Generates a Debian package for Linux.
      |    * `exe`: Generates a Windows .exe package.
      |    * `msi`: Generates a Windows Installer package.
      |
      | Defaults to `image`.
    """.stripMargin)

  val jdkAppIcon = SettingKey[Option[File]]("jdkAppIcon",
    """Path to platform-specific application icon:
      |    * `icns`: MacOS
      |    * `ico`: Windows
      |    * `png`: Linux
      |
      | Defaults to generic Java icon.
    """.stripMargin)

}
