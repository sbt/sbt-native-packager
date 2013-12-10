package com.typesafe.sbt
package packager
package archetypes

import Keys._
import sbt._
import sbt.Keys.{ target, mainClass, normalizedName }
import SbtNativePackager._
import com.typesafe.sbt.packager.linux.{ LinuxFileMetaData, LinuxPackageMapping }

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
  import ServerLoader._

  def settings: Seq[Setting[_]] = JavaAppPackaging.settings ++ debianSettings
  protected def etcDefaultTemplateSource: java.net.URL = getClass.getResource("etc-default-template")

  def debianSettings: Seq[Setting[_]] =
    Seq(
      debianStartScriptReplacements <<= (
        maintainer in Debian, packageSummary in Debian, serverLoading in Debian, daemonUser in Debian, normalizedName,
        sbt.Keys.version, defaultLinuxInstallLocation, mainClass in Compile, scriptClasspath)
        map { (author, descr, loader, daemonUser, name, version, installLocation, mainClass, cp) =>
          // TODO name-version is copied from UniversalPlugin. This should be consolidated into a setting (install location...)
          val appDir = installLocation + "/" + name
          val appClasspath = cp.map(appDir + "/lib/" + _).mkString(":")

          JavaAppStartScript.makeReplacements(
            author = author,
            description = descr,
            execScript = name,
            chdir = appDir,
            appName = name,
            appClasspath = appClasspath,
            appMainClass = mainClass.get,
            daemonUser = daemonUser)
        },
      debianMakeStartScript <<= (debianStartScriptReplacements, normalizedName, target in Universal, serverLoading in Debian)
        map makeDebianStartScript,
      debianMakeEtcDefault <<= (normalizedName, target in Universal, serverLoading in Debian)
        map makeEtcDefaultScript,
      linuxPackageMappings in Debian <++= (debianMakeEtcDefault, normalizedName) map { (conf, name) =>
        conf.map(c => LinuxPackageMapping(Seq(c -> s"/etc/default/$name")).withConfig()).toSeq
      },
      linuxPackageMappings in Debian <++= (debianMakeStartScript, normalizedName, serverLoading in Debian)
        map { (script, name, loader) =>
          val (path, permissions) = loader match {
            case Upstart => ("/etc/init/" + name + ".conf", "0644")
            case SystemV => ("/etc/init.d/" + name, "0755")
          }

          for {
            s <- script.toSeq
          } yield LinuxPackageMapping(Seq(s -> path)).withPerms(permissions)
        },
      // TODO - only make these if the upstart config exists...
      debianMakePrermScript <<= (normalizedName, target in Universal) map makeDebianPrermScript,
      debianMakePostinstScript <<= (normalizedName, target in Universal, serverLoading in Debian) map makeDebianPostinstScript)

  private def makeDebianStartScript(
    replacements: Seq[(String, String)], name: String, tmpDir: File, loader: ServerLoader): Option[File] =
    if (replacements.isEmpty) None
    else {
      val scriptBits = JavaAppStartScript.generateScript(replacements, loader)
      val script = tmpDir / "tmp" / "bin" / s"$name.$loader"
      IO.write(script, scriptBits)
      Some(script)
    }

  protected def makeDebianPrermScript(name: String, tmpDir: File): Option[File] = {
    val scriptBits = JavaAppStartScript.generatePrerm(name)
    val script = tmpDir / "tmp" / "bin" / "debian-prerm"
    IO.write(script, scriptBits)
    Some(script)
  }

  protected def makeDebianPostinstScript(name: String, tmpDir: File, loader: ServerLoader): Option[File] = {
    val scriptBits = JavaAppStartScript.generatePostinst(name, loader)
    val script = tmpDir / "tmp" / "bin" / "debian-postinst"
    IO.write(script, scriptBits)
    Some(script)
  }

  protected def makeEtcDefaultScript(name: String, tmpDir: File, loader: ServerLoader): Option[File] = {
    loader match {
      case Upstart => None
      case SystemV => {
        val scriptBits = TemplateWriter.generateScript(etcDefaultTemplateSource, Seq.empty)
        val script = tmpDir / "tmp" / "bin" / "etc-default"
        IO.write(script, scriptBits)
        Some(script)
      }
    }
  }
}
