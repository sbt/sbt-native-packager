package com.typesafe.sbt.packager.archetypes.jar

import java.io.File
import java.util.jar.Attributes

import sbt.Package.ManifestAttributes
import sbt.{*, given}
import sbt.Keys._
import com.typesafe.sbt.packager.PluginCompat
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

object ClasspathJarPlugin extends AutoPlugin {

  object autoImport {
    @transient
    val packageJavaClasspathJar: TaskKey[PluginCompat.FileRef] =
      taskKey[PluginCompat.FileRef]("Creates a Java classpath jar that specifies the classpath in its manifest")
  }
  import autoImport._

  override def requires = JavaAppPackaging

  override lazy val projectSettings: Seq[Setting[?]] = Defaults
    .packageTaskSettings(packageJavaClasspathJar, packageJavaClasspathJar / mappings) ++ Seq(
    packageJavaClasspathJar / mappings := Nil,
    packageJavaClasspathJar / artifactClassifier := Option("classpath"),
    packageJavaClasspathJar / packageOptions := {
      val classpath = (packageJavaClasspathJar / scriptClasspath).value
      val manifestClasspath = PluginCompat.classpathAttr -> classpath.mkString(" ")
      Seq(ManifestAttributes(manifestClasspath))
    },
    packageJavaClasspathJar / artifactName := { (scalaVersion, moduleId, artifact) =>
      moduleId.organization + "." + artifact.name + "-" + moduleId.revision +
        artifact.classifier.fold("")("-" + _) + "." + artifact.extension
    },
    bashScriptDefines / scriptClasspath :=
      Seq(PluginCompat.getArtifactPathName((packageJavaClasspathJar / artifactPath).value)),
    batScriptReplacements / scriptClasspath :=
      Seq(PluginCompat.getArtifactPathName((packageJavaClasspathJar / artifactPath).value)),
    Universal / mappings += {
      val classpathJar = packageJavaClasspathJar.value
      classpathJar -> ("lib/" + PluginCompat.getName(classpathJar))
    }
  )
}
