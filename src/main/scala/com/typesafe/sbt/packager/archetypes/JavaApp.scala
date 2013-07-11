package com.typesafe.sbt
package packager
package archetypes

import Keys._
import sbt._
import sbt.Keys.{mappings, target, name, mainClass, normalizedName}
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
    // Here we record the classpath as it's added to the mappings separately, so
    // we cna use it to generate the bash/bat scripts.
    classpathOrdering := Nil, 
    classpathOrdering <+= (Keys.packageBin in Compile) map { jar =>
	  jar -> ("lib/" + jar.getName)
	},
    classpathOrdering <++= (Keys.managedClasspath in Compile) map universalDepMappings,
    mappings in Universal <++= classpathOrdering,
    scriptClasspath <<= classpathOrdering map makeRelativeClasspathNames, 
    bashScriptExtraDefines := Nil,
    bashScriptDefines <<= (Keys.mainClass in Compile, scriptClasspath, bashScriptExtraDefines) map { (mainClass, cp, extras) =>
      val hasMain =
        for {
          cn <- mainClass
        } yield JavaAppBashScript.makeDefines(cn, appClasspath = cp, extras = extras)
      hasMain getOrElse Nil
    },
    makeBashScript <<= (bashScriptDefines, target in Universal, normalizedName) map makeUniversalBinScript,
    batScriptReplacements <<= (normalizedName, Keys.mainClass in Compile, scriptClasspath) map { (name, mainClass, cp) =>
      mainClass map { mc => 
        JavaAppBatScript.makeReplacements(name = name, mainClass = mc, appClasspath = cp)
      } getOrElse Nil
      
    },
    makeBatScript <<= (batScriptReplacements, target in Universal, normalizedName) map makeUniversalBatScript,
    mappings in Universal <++= (makeBashScript, normalizedName) map { (script, name) =>
      for {
        s <- script.toSeq  
      } yield s -> ("bin/" + name)
    },
    mappings in Universal <++= (makeBatScript, normalizedName) map { (script, name) =>
      for {
        s <- script.toSeq  
      } yield s -> ("bin/" + name + ".bat")
    } 
  )
  
  def makeRelativeClasspathNames(mappings: Seq[(File, String)]): Seq[String] =
    for {
      (file, name) <- mappings
    } yield {
      // Here we want the name relative to the lib/ folder...
      // For now we just cheat...
      if(name startsWith "lib/") name drop 4
      else "../" + name
    }
  
  def makeUniversalBinScript(defines: Seq[String], tmpDir: File, name: String): Option[File] = 
    if(defines.isEmpty) None
    else {
      val scriptBits = JavaAppBashScript.generateScript(defines)
      val script = tmpDir / "tmp" / "bin" / name
      IO.write(script, scriptBits)
      // TODO - Better control over this!
      script.setExecutable(true)
      Some(script)
    }
  
  def makeUniversalBatScript(replacements: Seq[(String, String)], tmpDir: File, name: String): Option[File] = 
    if(replacements.isEmpty) None
    else {
      val scriptBits = JavaAppBatScript.generateScript(replacements)
      val script = tmpDir / "tmp" / "bin" / (name + ".bat")
      IO.write(script, scriptBits)
      Some(script)
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