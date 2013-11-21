package com.typesafe.sbt
package packager
package archetypes

import Keys._
import sbt._
import sbt.Project.Initialize
import sbt.Keys.{ mappings, target, name, mainClass, normalizedName }
import linux.LinuxPackageMapping
import SbtNativePackager._
import com.typesafe.sbt.packager.linux.LinuxPackageMapping

/**
 * This class contains the default settings for creating and deploying an archetypical Java application.
 *  A Java application archetype is defined as a project that has a main method and is run by placing
 *  all of its JAR files on the classpath and calling that main method.
 *
 *  This doesn't create the best of distributions, but it can simplify the distribution of code.
 *
 *  **NOTE:  EXPERIMENTAL**   This currently only supports debian upstart scripts.
 */
object JavaServerAppPackaging extends JavaServerAppPackaging {

  def settings: Seq[Setting[_]] =
    JavaAppPackaging.settings ++
      debianUpstartSettings

  def debianUpstartSettings: Seq[Setting[_]] =
    Seq(
      debianUpstartScriptReplacements <<= (maintainer in Debian, packageSummary in Debian, normalizedName, sbt.Keys.version, defaultLinuxInstallLocation) map { (author, descr, name, version, installLocation) =>
        // TODO name-version is copied from UniversalPlugin. This should be consolidated into a setting (install location...)
        val chdir = installLocation + "/" + name + "/bin"
        JavaAppUpstartScript.makeReplacements(author = author, descr = descr, execScript = name, chdir = chdir)
      },
      debianMakeUpstartScript <<= (debianUpstartScriptReplacements, normalizedName, target in Universal) map makeDebianUpstartScript,
      linuxPackageMappings in Debian <++= (debianMakeUpstartScript, normalizedName) map { (script, name) =>
        for {
          s <- script.toSeq
        } yield LinuxPackageMapping(Seq(s -> ("/etc/init/" + name + ".conf"))).withPerms("0644")
      },
      // TODO - only make these if the upstart config exists...
      debianMakePrermScript <<= (normalizedName, target in Universal) map makeDebianPrermScript,
      debianMakePostinstScript <<= (normalizedName, target in Universal) map makeDebianPostinstScript)


  protected final def makeDebianUpstartScript(replacements: Seq[(String, String)], name: String, tmpDir: File): Option[File] =
    if (replacements.isEmpty) None
    else {
      val scriptBits = JavaAppUpstartScript.generateScript(replacements)
      val script = tmpDir / "tmp" / "bin" / (name + ".conf")
      IO.write(script, scriptBits)
      Some(script)
    }
}


object JavaServerAppSysVinitPackaging extends JavaServerAppPackaging {

  def settings: Seq[Setting[_]] =
    JavaAppPackaging.settings ++ debianSysVinitSettings

  def debianSysVinitSettings: Seq[Setting[_]] = {
    Seq(
      debianSysVinitScriptReplacements <<= (maintainer in Debian, packageSummary in Debian,
        normalizedName, name, sbt.Keys.version, defaultLinuxInstallLocation, sbt.Keys.mainClass in Compile, scriptClasspath)
          map { (author, descr, normalizedName, name, version, installLocation, mainClass, cp) =>
      // TODO name-version is copied from UniversalPlugin. This should be consolidated into a setting (install location...)
        val appDir = installLocation + "/" + normalizedName
        val appClasspath = cp.map(appDir + "/lib/" + _).mkString(":")

        JavaAppSysVinitScript.makeReplacements(
          author = author, description = descr,
          appDir = appDir,
          appName = name,
          appClasspath = appClasspath,
          appMainClass = mainClass.getOrElse("") //TODO: is it possible
        )
      },
      debianMakeSysVinitScript <<= (debianSysVinitScriptReplacements, normalizedName, target in Universal) map makeDebianSysVinitScript,
      linuxPackageMappings in Debian <++= (debianMakeSysVinitScript, normalizedName) map { (script, name) =>
        for {
          s <- script.toSeq
        } yield LinuxPackageMapping(Seq(s -> ("/etc/init.d/" + name))).withPerms("0755")
      },
      // TODO - only make these if the upstart config exists...
      debianMakePrermScript <<= (normalizedName, target in Universal) map makeDebianPrermScript,
      debianMakePostinstScript <<= (normalizedName, target in Universal) map makeDebianPostinstScript)
  }


  protected final def makeDebianSysVinitScript(replacements: Seq[(String, String)], name: String, tmpDir: File): Option[File] =
    if (replacements.isEmpty) None
    else {
      val scriptBits = JavaAppSysVinitScript.generateScript(replacements)
      val script = tmpDir / "tmp" / "bin" / (name + ".conf")
      IO.write(script, scriptBits)
      Some(script)
    }
}


trait JavaServerAppPackaging {

  def settings: Seq[Setting[_]]

  protected def makeDebianPrermScript(name: String, tmpDir: File): Option[File] = {
    val scriptBits = JavaAppUpstartScript.generatePrerm(name)
    val script = tmpDir / "tmp" / "bin" / "debian-prerm"
    IO.write(script, scriptBits)
    Some(script)
  }

  protected def makeDebianPostinstScript(name: String, tmpDir: File): Option[File] = {
    val scriptBits = JavaAppUpstartScript.generatePostinst(name)
    val script = tmpDir / "tmp" / "bin" / "debian-postinst"
    IO.write(script, scriptBits)
    Some(script)
  }
}
