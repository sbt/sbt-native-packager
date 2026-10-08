package com.typesafe.sbt.packager.archetypes.jar

import sbt.Package.ManifestAttributes
import sbt.{*, given}
import sbt.Keys._
import com.typesafe.sbt.packager.Compat.*
import com.typesafe.sbt.packager.PluginCompat
import sbtcompat.PluginCompat._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import xsbti.FileConverter

object LauncherJarPlugin extends AutoPlugin {

  object autoImport {
    @transient
    val packageJavaLauncherJar: TaskKey[FileRef] =
      taskKey[FileRef]("Creates a Java launcher jar that specifies the main class and classpath in its manifest")
  }

  import autoImport._

  override def requires = JavaAppPackaging

  override lazy val projectSettings: Seq[Setting[?]] = Defaults
    .packageTaskSettings(packageJavaLauncherJar, packageJavaLauncherJar / mappings) ++ Seq(
    packageJavaLauncherJar / mappings := Nil,
    packageJavaLauncherJar / artifactClassifier := Option("launcher"),
    packageJavaLauncherJar / packageOptions := Def.uncached {
      val classpath = (packageJavaLauncherJar / scriptClasspath).value
      val manifestClasspath = PluginCompat.classpathAttr -> classpath.mkString(" ")
      val manifestMainClass =
        (Compile / packageJavaLauncherJar / mainClass).value.map(PluginCompat.mainclassAttr -> _)
      Seq(ManifestAttributes((manifestMainClass.toSeq :+ manifestClasspath)*))
    },
    packageJavaLauncherJar / artifactName := { (scalaVersion, moduleId, artifact) =>
      moduleId.organization + "." + artifact.name + "-" + moduleId.revision +
        artifact.classifier.fold("")("-" + _) + "." + artifact.extension
    },
    Compile / bashScriptDefines / mainClass := Def.uncached {
      implicit val conv: FileConverter = fileConverter.value
      val a = (packageJavaLauncherJar / artifactPath).value
      Some(s"""-jar "$$lib_dir/${artifactPathToFile(a).getName}"""")
    },
    bashScriptDefines / scriptClasspath := Nil,
    Compile / batScriptReplacements / mainClass := Def.uncached {
      implicit val conv: FileConverter = fileConverter.value
      val a = (packageJavaLauncherJar / artifactPath).value
      Some(s"""-jar "%APP_LIB_DIR%\\${artifactPathToFile(a).getName}"""")
    },
    batScriptReplacements / scriptClasspath := Nil,
    Universal / mappings += {
      val javaLauncher = packageJavaLauncherJar.value
      implicit val conv: FileConverter = fileConverter.value
      javaLauncher -> ("lib/" + toFile(javaLauncher).getName)
    }
  )
}
