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
object JavaServerAppPackaging {

  def settings: Seq[Setting[_]] =
    JavaAppPackaging.settings ++
      debianUpstartSettings

  def debianUpstartSettings: Seq[Setting[_]] =
    Seq(
      debianUpstartScriptReplacements <<= (maintainer in Debian, packageSummary in Debian, normalizedName, sbt.Keys.version) map { (author, descr, name, version) =>
        // TODO name-version is copied from UniversalPlugin. This should be consolidated into a setting (install location...)
        val chdir = GenericPackageSettings.installLocation + "/" + name + "/bin"
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

  private[this] final def makeDebianPrermScript(name: String, tmpDir: File): Option[File] = {
    val scriptBits = JavaAppUpstartScript.generatePrerm(name)
    val script = tmpDir / "tmp" / "bin" / "debian-prerm"
    IO.write(script, scriptBits)
    Some(script)
  }

  private[this] final def makeDebianPostinstScript(name: String, tmpDir: File): Option[File] = {
    val scriptBits = JavaAppUpstartScript.generatePostinst(name)
    val script = tmpDir / "tmp" / "bin" / "debian-postinst"
    IO.write(script, scriptBits)
    Some(script)
  }

  private[this] final def makeDebianUpstartScript(replacements: Seq[(String, String)], name: String, tmpDir: File): Option[File] =
    if (replacements.isEmpty) None
    else {
      val scriptBits = JavaAppUpstartScript.generateScript(replacements)
      val script = tmpDir / "tmp" / "bin" / (name + ".conf")
      IO.write(script, scriptBits)
      Some(script)
    }
}