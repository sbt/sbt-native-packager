package com.typesafe.sbt.packager.archetypes.jar

import java.io.File
import java.util.jar.Attributes

import sbt.Package.ManifestAttributes
import sbt.{*, given}
import sbt.Keys._
import com.typesafe.sbt.packager.Compat.*
import com.typesafe.sbt.packager.PluginCompat
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import xsbti.FileConverter

object LauncherJarPlugin extends AutoPlugin {

  object autoImport {
    @transient
    val packageJavaLauncherJar: TaskKey[PluginCompat.FileRef] = taskKey[PluginCompat.FileRef](
      "Creates a Java launcher jar that specifies the main class and classpath in its manifest"
    )
  }

  import autoImport._

  override def requires = JavaAppPackaging

  override lazy val projectSettings: Seq[Setting[?]] = Defaults
    .packageTaskSettings(packageJavaLauncherJar, packageJavaLauncherJar / mappings) ++ Seq(
    packageJavaLauncherJar / mappings := Nil,
    packageJavaLauncherJar / artifactClassifier := Option("launcher"),
    packageJavaLauncherJar / packageOptions := {
      val classpath = (packageJavaLauncherJar / scriptClasspath).value
      val manifestClasspath = PluginCompat.classpathAttr -> classpath.mkString(" ")
      val manifestMainClass =
        (Compile / packageJavaLauncherJar / mainClass).value.map(PluginCompat.mainclassAttr -> _)
      Seq(ManifestAttributes(manifestMainClass.toSeq :+ manifestClasspath: _*))
    },
    packageJavaLauncherJar / artifactName := { (scalaVersion, moduleId, artifact) =>
      moduleId.organization + "." + artifact.name + "-" + moduleId.revision +
        artifact.classifier.fold("")("-" + _) + "." + artifact.extension
    },
    Compile / bashScriptDefines / mainClass := Def.uncached {
      val conv0 = fileConverter.value
      implicit val conv: FileConverter = conv0
      val a = (packageJavaLauncherJar / artifactPath).value
      Some(s"""-jar "$$lib_dir/${PluginCompat.artifactPathToFile(a).getName}"""")
    },
    bashScriptDefines / scriptClasspath := Nil,
    Compile / batScriptReplacements / mainClass := Def.uncached {
      val conv0 = fileConverter.value
      implicit val conv: FileConverter = conv0
      val a = (packageJavaLauncherJar / artifactPath).value
      Some(s"""-jar "%APP_LIB_DIR%\\${PluginCompat.artifactPathToFile(a).getName}"""")
    },
    batScriptReplacements / scriptClasspath := Nil,
    Universal / mappings += {
      val javaLauncher = packageJavaLauncherJar.value
      val conv0 = fileConverter.value
      implicit val conv: FileConverter = conv0
      javaLauncher -> ("lib/" + PluginCompat.toFile(javaLauncher).getName)
    }
  )
}
