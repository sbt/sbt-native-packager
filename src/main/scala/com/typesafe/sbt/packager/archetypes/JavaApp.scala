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
  
  def settings =
    defaultUniversalSettings ++
    defaultLinuxSettings
    
  /// Universal packaging defaults.
  def defaultUniversalSettings: Seq[Setting[_]] = Seq(
    mappings in Universal <++= (Keys.managedClasspath in Compile) map universalDepMappings,
    mappings in Universal <+= (Keys.packageBin in Compile) map { jar =>
      jar -> ("lib/" + jar.getName)
    },
    mappings in Universal <++= (Keys.mainClass in Compile, target in Universal, name in Universal) map makeUniversalBinScript 
  )
  
  def makeUniversalBinScript(mainClass: Option[String], tmpDir: File, name: String): Seq[(File, String)] = 
    for(mc <- mainClass.toSeq) yield {
      val scriptBits = JavaAppBashScript.generateScript(mc)
      val script = tmpDir / "tmp" / "bin" / name
      IO.write(script, scriptBits)
      script -> ("bin/" + name)
    }
  
  // Converts a managed classpath into a set of lib mappings.
  def universalDepMappings(deps: Seq[Attributed[File]]): Seq[(File,String)] = 
    for {
      dep <- deps
      file = dep.data
      // TODO - Figure out what to do with jar files.
      if file.isFile
    } yield dep.data -> ("lib/" + dep.data.getName)
    
    
  // Default linux settings are driven off of the universal settings.  
  def defaultLinuxSettings: Seq[Setting[_]] = Seq(
    linuxPackageMappings <+= (mappings in Universal, name in Linux) map filterLibs,
    linuxPackageMappings <++= (mainClass in Compile, name in Linux, target in Linux) map makeLinuxBinScrit
  )
  
  def filterLibs(mappings: Seq[(File, String)], name: String): LinuxPackageMapping = {
    val libs = for {
      (file, location) <- mappings
      if location startsWith "lib/"
    } yield file -> ("/usr/share/"+name+"/" + location)
    packageMapping(libs:_*)
  }
  
    
  def makeLinuxBinScrit(mainClass: Option[String], name: String, tmpDir: File): Seq[LinuxPackageMapping] =
    for(mc <- mainClass.toSeq) yield {
      val scriptBits = JavaAppBashScript.generateScript(
          mainClass = mc,
          libDir = "/usr/share/" + name + "/lib")
      val script = tmpDir / "tmp" / "bin" / name
      IO.write(script, scriptBits)
      val scriptMapping = script -> ("/usr/bin/" + name)
      
      packageMapping(scriptMapping).withPerms("0755")
    }
}