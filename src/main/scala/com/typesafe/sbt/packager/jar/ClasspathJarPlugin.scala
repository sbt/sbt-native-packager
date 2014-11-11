package com.typesafe.sbt.packager
package archetypes.jar

import sbt._
import sbt.Keys.{ mappings, target, name, mainClass, sourceDirectory }
import com.typesafe.sbt.packager.Keys.{ scriptClasspath }
import com.typesafe.sbt.SbtNativePackager.{ Universal }

object ClasspathJarPlugin extends AutoPlugin {

  object autoImport {
    val classspathJarName = TaskKey[String]("classpath-jar-name", "classpath-jar name")
  }
  import autoImport._

  override def requires = archetypes.JavaAppPackaging

  override lazy val projectSettings = Seq[Setting[_]](
    classspathJarName <<= (Keys.packageBin in Compile, Keys.projectID, Keys.artifact in Compile in Keys.packageBin) map { (jar, id, art) =>
      makeJarName(id.organization, id.name, id.revision, art.name, art.classifier)
    },
    mappings in Universal += {
      ((target in Universal).value / "lib" / classspathJarName.value) -> ("lib/" + classspathJarName.value)
    },
    scriptClasspath := {
      writeJar((target in Universal).value / "lib" / classspathJarName.value, scriptClasspath.value)
      Seq(classspathJarName.value)
    }
  )

  // Constructs a jar name from components...(ModuleID/Artifact)
  private def makeJarName(org: String, name: String, revision: String, artifactName: String, artifactClassifier: Option[String]): String =
    (org + "." +
      name + "-" +
      Option(artifactName.replace(name, "")).filterNot(_.isEmpty).map(_ + "-").getOrElse("") +
      revision +
      artifactClassifier.filterNot(_.isEmpty).map("-" + _).getOrElse("") +
      "-classpath.jar")

  /** write jar file */
  private def writeJar(jarFile: File, allClasspath: Seq[String]) = {
    val manifest = new java.util.jar.Manifest()
    manifest.getMainAttributes().putValue("Class-Path", allClasspath.mkString(" "))
    IO.jar(Seq.empty, jarFile, manifest)
  }
}
