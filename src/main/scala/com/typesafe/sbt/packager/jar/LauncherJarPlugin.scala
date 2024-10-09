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
    .packageTaskSettings(packageJavaLauncherJar, packageJavaLauncherJar / mappings ) ++ Seq(
    packageJavaLauncherJar / mappings  := Nil,
    packageJavaLauncherJar / artifactClassifier  := Option("launcher"),
    packageJavaLauncherJar / packageOptions  := {
      val classpath = (packageJavaLauncherJar / scriptClasspath ).value
      val manifestClasspath = Attributes.Name.CLASS_PATH -> classpath.mkString(" ")
      val manifestMainClass =
        (Compile / packageJavaLauncherJar / mainClass).value.map(Attributes.Name.MAIN_CLASS -> _)
      Seq(ManifestAttributes(manifestMainClass.toSeq :+ manifestClasspath: _*))
    },
    packageJavaLauncherJar / artifactName  := { (scalaVersion, moduleId, artifact) =>
      moduleId.organization + "." + artifact.name + "-" + moduleId.revision +
        artifact.classifier.fold("")("-" + _) + "." + artifact.extension
    },
    Compile / bashScriptDefines / mainClass := {
      Some(s"""-jar "$$lib_dir/${(packageJavaLauncherJar / artifactPath ).value.getName}"""")
    },
    bashScriptDefines / scriptClasspath  := Nil,
    Compile / batScriptReplacements / mainClass := {
      Some(s"""-jar "%APP_LIB_DIR%\\${(packageJavaLauncherJar / artifactPath ).value.getName}"""")
    },
    batScriptReplacements / scriptClasspath := Nil,
    Universal / mappings += {
      val javaLauncher = packageJavaLauncherJar.value
      javaLauncher -> ("lib/" + javaLauncher.getName)
    }
  )
}
