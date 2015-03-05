package com.typesafe.sbt.packager
package archetypes.jar

import java.io.File
import java.util.jar.Attributes

import sbt.Package.ManifestAttributes
import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.SbtNativePackager.Universal

object ClasspathJarPlugin extends AutoPlugin {

  object autoImport {
    val packageJavaClasspathJar = TaskKey[File]("packageJavaClasspathJar", "Creates a Java classpath jar that specifies the classpath in its manifest")
  }
  import autoImport._

  override def requires = archetypes.JavaAppPackaging

  override lazy val projectSettings = Defaults.packageTaskSettings(packageJavaClasspathJar, mappings in packageJavaClasspathJar) ++ Seq(
    mappings in packageJavaClasspathJar := Nil,

    artifactClassifier in packageJavaClasspathJar := Option("classpath"),

    packageOptions in packageJavaClasspathJar := {
      val classpath = (scriptClasspath in packageJavaClasspathJar).value
      val manifestClasspath = Attributes.Name.CLASS_PATH -> classpath.mkString(" ")
      Seq(
        ManifestAttributes(manifestClasspath)
      )
    },

    artifactName in packageJavaClasspathJar := { (scalaVersion, moduleId, artifact) =>
      moduleId.organization + "." + artifact.name + "-" + moduleId.revision +
        artifact.classifier.fold("")("-" + _) + "." + artifact.extension
    },

    scriptClasspath in bashScriptDefines := Seq(
      (artifactPath in packageJavaClasspathJar).value.getName
    ),

    scriptClasspath in batScriptReplacements := Seq(
      (artifactPath in packageJavaClasspathJar).value.getName
    ),

    mappings in Universal += {
      val classpathJar = packageJavaClasspathJar.value
      classpathJar -> ("lib/" + classpathJar.getName)
    }

  )
}
