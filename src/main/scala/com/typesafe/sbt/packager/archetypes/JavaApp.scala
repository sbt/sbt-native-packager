package com.typesafe.sbt
package packager
package archetypes

import Keys._
import sbt._
import sbt.Keys.{mappings, target, name, mainClass}
import linux.LinuxPackageMapping
import SbtNativePackager._

/** This class contains the default settings for creating and deploying an archetypical Java application.
 *  A Java application archetype is defined as a project that has a main method and is run by placing
 *  all of its JAR files on the classpath and calling that main method.
 *  
 *  This doesn't create the best of distributions, but it can simplify the distribution of code.
 *  
 *  **NOTE:  EXPERIMENTAL**   This currently only supports universal distributions.
 */
object JavaAppPackaging {
  
  def settings: Seq[Setting[_]] = Seq(
    mappings in Universal <++= (Keys.managedClasspath in Compile) map universalDepMappings,
    mappings in Universal <+= (Keys.packageBin in Compile) map { jar =>
      jar -> ("lib/" + jar.getName)
    },
    makeBashScript <<= (Keys.mainClass in Compile, target in Universal, name in Universal) map makeUniversalBinScript,
    makeBatScript <<= (Keys.mainClass in Compile, target in Universal, name) map makeUniversalBatScript,
    mappings in Universal <++= (makeBashScript, name) map { (script, name) =>
      for {
        s <- script.toSeq  
      } yield s -> ("bin/" + name)
    },
    mappings in Universal <++= (makeBatScript, name) map { (script, name) =>
      for {
        s <- script.toSeq  
      } yield s -> ("bin/" + name + ".bat")
    } 
  )
  
  def makeUniversalBinScript(mainClass: Option[String], tmpDir: File, name: String): Option[File] = 
    for(mc <- mainClass) yield {
      val scriptBits = JavaAppBashScript.generateScript(mc)
      val script = tmpDir / "tmp" / "bin" / name
      IO.write(script, scriptBits)
      // TODO - Better control over this!
      script.setExecutable(true)
      script
    }
  
  def makeUniversalBatScript(mainClass: Option[String], tmpDir: File, name: String): Option[File] = 
    for(mc <- mainClass) yield {
      val scriptBits = JavaAppBatScript.generateScript(name, mc)
      val script = tmpDir / "tmp" / "bin" / (name + ".bat")
      IO.write(script, scriptBits)
      script
    }

  // Converts a managed classpath into a set of lib mappings.
  def universalDepMappings(deps: Seq[Attributed[File]]): Seq[(File,String)] = 
    for {
      dep <- deps
      file = dep.data
      if file.isFile
      // TODO - Figure out what to do with jar files.
    } yield {
      val filename: Option[String] = for {
            module <- dep.metadata.get(AttributeKey[ModuleID]("module-id"))
            artifact <- dep.metadata.get(AttributeKey[Artifact]("artifact"))
          } yield {
            module.organization + "." +
              module.name + "-" +
              Option(artifact.name.replace(module.name, "")).filterNot(_.isEmpty).map(_ + "-").getOrElse("") +
              module.revision + ".jar"
      }
        
      dep.data -> ("lib/" + filename.getOrElse(file.getName))
    }
}