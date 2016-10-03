package com.typesafe.sbt.packager.jdkpackager

import com.typesafe.sbt.SbtNativePackager
import com.typesafe.sbt.packager.jdkpackager.JDKPackagerPlugin.autoImport._
import sbt._

/**
  * Keys specific to deployment via the `javapackger` tool.
  *
  * @author <a href="mailto:fitch@datamininglab.com">Simeon H.K. Fitch</a>
  * @since 2/11/15
  */
trait JDKPackagerKeys {

  val jdkPackagerBasename = settingKey[String](
    "Filename sans extension for generated installer package.")

  val jdkPackagerType = settingKey[String](
    """Value passed as the `native` attribute to `fx:deploy` task.
      |Per `javapackager` documentation, this may be one of the following:
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
      | Default: `installer`.
      | Details:
      |   http://docs.oracle.com/javase/8/docs/technotes/guides/deploy/javafx_ant_task_reference.html#CIABIFCI
    """.stripMargin
  )

  val jdkPackagerToolkit = settingKey[JDKPackagerToolkit](
    "GUI toolkit used in app. Either `JavaFXToolkit` (default) or `SwingToolkit`"
  )

  val jdkPackagerJVMArgs = settingKey[Seq[String]](
    """Sequence of arguments to pass to the JVM.
      |Default: `Seq("-Xmx768m")`.
      |Details:
      |   http://docs.oracle.com/javase/8/docs/technotes/guides/deploy/javafx_ant_task_reference.html#CIAHJIJG
    """.stripMargin
  )

  val jdkPackagerAppArgs = settingKey[Seq[String]](
    """List of command line arguments to pass to the application on launch.
      |Default: `Seq.empty`
      |Details:
      |   http://docs.oracle.com/javase/8/docs/technotes/guides/deploy/javafx_ant_task_reference.html#CACIJFHB
      |
    """.stripMargin
  )

  val jdkPackagerProperties = settingKey[Map[String, String]](
    """Map of `System` properties to define in application.
      |Default: `Map.empty`
      |Details:
      |  http://docs.oracle.com/javase/8/docs/technotes/guides/deploy/javafx_ant_task_reference.html#CIAHCIFJ
    """.stripMargin
  )

  val jdkAppIcon = settingKey[Option[File]](
    """Path to platform-specific application icon:
      |    * `icns`: MacOS
      |    * `ico`: Windows
      |    * `png`: Linux
      |
      | Defaults to generic Java icon.
    """.stripMargin
  )

  val jdkPackagerAssociations = settingKey[Seq[FileAssociation]](
    """Set of application file associations to register for the application.
      |Example: `jdkPackagerAssociations := Seq(FileAssociation("foo", "application/x-foo", Foo Data File", iconPath))
      |Default: `Seq.empty`
      |Note: Requires JDK >= 8 build 40.
      |Details:
      |  http://docs.oracle.com/javase/8/docs/technotes/guides/deploy/javafx_ant_task_reference.html#CIAIDHBJ
    """.stripMargin
  )

  /** Config for scoping keys outside of Global . */
  val JDKPackager = config("jdkPackager") extend SbtNativePackager.Universal

  // ------------------------------------------
  // Keys to be defined in JDKPackager config.
  // ------------------------------------------

  val antPackagerTasks = settingKey[Option[File]](
    "Path to `ant-javafx.jar` library in JDK. By plugin attempts to find location based on `java.home` property. Specifying `JAVA_HOME` or `JDK_HOME` can help."
  )

  val antBuildDefn = taskKey[xml.Node](
    "Generates a Ant XML DOM defining package generating build for JDK provided Ant task."
  )

  val writeAntBuild = taskKey[File](
    "Write the Ant `build.xml` file to the jdkpackager target directory"
  )

  val antExtraClasspath = settingKey[Seq[File]](
    "Additional classpath entries for the JavaFX Ant task beyond `antPackagerTasks`"
  )
}
