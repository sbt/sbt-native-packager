package com.typesafe.sbt.packager.jdkpackager

import com.typesafe.sbt.SbtNativePackager
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.SettingsHelper
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.archetypes.jar.LauncherJarPlugin
import sbt.Keys._
import sbt._
import SbtNativePackager.Universal
import JDKPackagerAntHelper._

/**
 * Package format via Oracle's packaging tool bundled with JDK 8.
 *
 * @author <a href="mailto:fitch@datamininglab.com">Simeon H.K. Fitch</a>
 * @since 2/11/15
 */
object JDKPackagerPlugin extends AutoPlugin {

  object autoImport extends JDKPackagerKeys {
    sealed trait JDKPackagerToolkit { def arg: String }
    case object SwingToolkit extends JDKPackagerToolkit { val arg = "swing" }
    case object JavaFXToolkit extends JDKPackagerToolkit { val arg = "fx" }

    case class FileAssociation(extension: String, mimetype: String,
      description: String, icon: Option[File] = None)
  }

  import autoImport._
  override def requires = JavaAppPackaging && LauncherJarPlugin
  private val dirname = JDKPackager.name.toLowerCase

  override def projectConfigurations: Seq[Configuration] = Seq(JDKPackager)

  override lazy val projectSettings = Seq(
    jdkAppIcon := None,
    jdkPackagerType := "installer",
    jdkPackagerBasename := packageName.value + "-pkg",
    jdkPackagerToolkit := JavaFXToolkit,
    jdkPackagerJVMArgs := Seq("-Xmx768m"),
    jdkPackagerAppArgs := Seq.empty,
    jdkPackagerProperties := Map.empty,
    jdkPackagerAssociations := Seq.empty
  ) ++ inConfig(JDKPackager)(
      Seq(
        sourceDirectory := sourceDirectory.value / "deploy",
        target := target.value / dirname,
        mainClass := (mainClass in Runtime).value,
        name := name.value,
        packageName := packageName.value,
        maintainer := maintainer.value,
        packageSummary := packageSummary.value,
        packageDescription := packageDescription.value,
        mappings := (mappings in Universal).value,
        antPackagerTasks := locateAntTasks(javaHome.value, sLog.value),
        antExtraClasspath := Seq(sourceDirectory.value, target.value),
        antBuildDefn := makeAntBuild(
          antPackagerTasks.value,
          antExtraClasspath.value,
          name.value,
          (stage in Universal).value,
          mappings.value,
          platformDOM(
            jdkPackagerJVMArgs.value,
            jdkPackagerProperties.value
          ),
          applicationDOM(
            name.value,
            version.value,
            mainClass.value,
            jdkPackagerToolkit.value,
            jdkPackagerAppArgs.value
          ),
          deployDOM(
            jdkPackagerBasename.value,
            jdkPackagerType.value,
            (artifactPath in LauncherJarPlugin.autoImport.packageJavaLauncherJar).value,
            target.value,
            infoDOM(
              name.value,
              packageDescription.value,
              maintainer.value,
              jdkAppIcon.value,
              jdkPackagerAssociations.value
            )
          )
        ),
        writeAntBuild := writeAntFile(
          target.value,
          antBuildDefn.value,
          streams.value
        ),
        packageBin := buildPackageWithAnt(
          writeAntBuild.value,
          target.value,
          streams.value
        )
      )
    )

}

/** Generates Ivy configuration to enable packaging artifact deployment. */
object JDKPackagerDeployPlugin extends AutoPlugin {
  import JDKPackagerPlugin.autoImport._
  override def requires = JDKPackagerPlugin

  override def projectSettings =
    SettingsHelper.makeDeploymentSettings(JDKPackager, packageBin in JDKPackager, "jdkPackager")
}
