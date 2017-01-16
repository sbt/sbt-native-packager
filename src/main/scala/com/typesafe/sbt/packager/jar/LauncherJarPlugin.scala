package com.typesafe.sbt.packager.archetypes.jar

import java.io.File
import java.util.jar.Attributes

import sbt.Package.ManifestAttributes
import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

object LauncherJarPlugin extends AutoPlugin {

  object autoImport {
    val packageJavaLauncherJar: TaskKey[File] = TaskKey[File](
      "packageJavaLauncherJar",
      "Creates a Java launcher jar that specifies the main class and classpath in its manifest"
    )
  }

  import autoImport._

  override def requires = JavaAppPackaging

  override lazy val projectSettings: Seq[Setting[_]] = Defaults
      .packageTaskSettings(packageJavaLauncherJar, mappings in packageJavaLauncherJar) ++ Seq(
      mappings in packageJavaLauncherJar := Nil,
      artifactClassifier in packageJavaLauncherJar := Option("launcher"),
      packageOptions in packageJavaLauncherJar := {
      val classpath = (scriptClasspath in packageJavaLauncherJar).value
      val manifestClasspath = Attributes.Name.CLASS_PATH -> classpath.mkString(" ")
      val manifestMainClass =
        (mainClass in (Compile, packageJavaLauncherJar)).value.map(Attributes.Name.MAIN_CLASS -> _)
      Seq(ManifestAttributes(manifestMainClass.toSeq :+ manifestClasspath: _*))
    },
      artifactName in packageJavaLauncherJar := { (scalaVersion, moduleId, artifact) =>
      moduleId.organization + "." + artifact.name + "-" + moduleId.revision +
        artifact.classifier.fold("")("-" + _) + "." + artifact.extension
    },
      mainClass in (Compile, bashScriptDefines) := {
      Some("-jar $lib_dir/" + (artifactPath in packageJavaLauncherJar).value.getName)
    },
      scriptClasspath in bashScriptDefines := Nil,
      mainClass in (Compile, batScriptReplacements) := {
      Some("-jar %APP_LIB_DIR%\\" + (artifactPath in packageJavaLauncherJar).value.getName)
    },
      scriptClasspath in batScriptReplacements := Nil,
      mappings in Universal += {
      val javaLauncher = packageJavaLauncherJar.value
      javaLauncher -> ("lib/" + javaLauncher.getName)
    }
    )
}
