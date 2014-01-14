package com.typesafe.sbt
package packager
package archetypes

import Keys._
import sbt._
import sbt.Keys.{ target, mainClass, normalizedName, sourceDirectory }
import SbtNativePackager._
import com.typesafe.sbt.packager.linux.{ LinuxFileMetaData, LinuxPackageMapping, LinuxSymlink, LinuxPlugin }

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
  import LinuxPlugin.Users

  def settings: Seq[Setting[_]] = JavaAppPackaging.settings ++ debianSettings
  protected def etcDefaultTemplateSource: java.net.URL = getClass.getResource("etc-default-template")

  def debianSettings: Seq[Setting[_]] =
    Seq(
      serverLoading := Upstart,
      daemonUser := Users.Root,
      // This one is begging for sbt 0.13 syntax...
      debianStartScriptReplacements <<= (
        maintainer in Debian, packageSummary in Debian, serverLoading in Debian, daemonUser in Debian, normalizedName,
        sbt.Keys.version, defaultLinuxInstallLocation, mainClass in Compile, scriptClasspath)
        map { (author, descr, loader, daemonUser, name, version, installLocation, mainClass, cp) =>
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
      // TODO - Default locations shouldn't be so hacky.
      linuxStartScriptTemplate in Debian <<= (serverLoading in Debian, sourceDirectory) map { (loader, dir) =>
        JavaAppStartScript.defaultStartScriptTemplate(loader, dir / "templates" / "start")
      },
      debianMakeStartScript <<= (debianStartScriptReplacements, normalizedName, target in Universal, linuxStartScriptTemplate in Debian)
        map makeDebianStartScript,
      linuxEtcDefaultTemplate in Debian <<= sourceDirectory map { dir =>
        val overrideScript = dir / "templates" / "etc-default"
        if (overrideScript.exists) overrideScript.toURI.toURL
        else etcDefaultTemplateSource
      },
      debianMakeEtcDefault <<= (normalizedName, target in Universal, serverLoading in Debian, linuxEtcDefaultTemplate in Debian)
        map makeEtcDefaultScript,
      linuxPackageMappings in Debian <++= (debianMakeEtcDefault, normalizedName) map { (conf, name) =>
        conf.map(c => LinuxPackageMapping(Seq(c -> ("/etc/default/" + name))).withConfig()).toSeq
      },
      linuxPackageMappings in Debian <++= (debianMakeStartScript, normalizedName, serverLoading in Debian)
        map { (script, name, loader) =>
          val (path, permissions) = loader match {
            case Upstart => ("/etc/init/" + name + ".conf", "0644")
            case SystemV => ("/etc/init.d/" + name, "0755")
          }
          for {
            s <- script.toSeq
          } yield LinuxPackageMapping(Seq(s -> path)).withPerms(permissions).withConfig()
        },
      // TODO should we specify daemonGroup in configs?
      linuxPackageMappings in Debian <+= (normalizedName, defaultLinuxLogsLocation, target in Debian, daemonUser in Debian) map {
        (name, logsDir, target, user) =>
          // create empty var/log directory
          val d = target / logsDir
          d.mkdirs()
          LinuxPackageMapping(Seq(d -> (logsDir + "/" + name)), LinuxFileMetaData(user, user))
      },
      linuxPackageSymlinks in Debian <+= (normalizedName, defaultLinuxInstallLocation) map {
        (name, install) => LinuxSymlink(install + "/" + name + "/logs", "/var/log/" + name)
      },
      // TODO - only make these if the upstart config exists...
      debianMakePrermScript <<= (normalizedName, target in Universal, serverLoading in Debian) map makeDebianPrermScript,
      debianMakePostrmScript <<= (normalizedName, target in Universal, serverLoading in Debian) map makeDebianPostrmScript,
      debianMakePostinstScript <<= (normalizedName, target in Universal, serverLoading in Debian) map makeDebianPostinstScript)

  private def makeDebianStartScript(
    replacements: Seq[(String, String)], name: String, tmpDir: File, template: URL): Option[File] =
    if (replacements.isEmpty) None
    else {
      val scriptBits = TemplateWriter.generateScript(template, replacements)
      val script = tmpDir / "tmp" / "init" / name
      IO.write(script, scriptBits)
      Some(script)
    }

  protected def makeDebianPrermScript(name: String, tmpDir: File, loader: ServerLoader): Option[File] = {
    val scriptBits = JavaAppStartScript.generatePrerm(loader, name)
    val script = tmpDir / "tmp" / "bin" / "debian-prerm"
    IO.write(script, scriptBits)
    Some(script)
  }

  protected def makeDebianPostrmScript(name: String, tmpDir: File, loader: ServerLoader): Option[File] = {
    JavaAppStartScript.generatePostrm(name, loader) match {
      case Some(scriptBits) =>
        val script = tmpDir / "tmp" / "bin" / "debian-postrm"
        IO.write(script, scriptBits)
        Some(script)
      case None => None
    }
  }

  protected def makeDebianPostinstScript(name: String, tmpDir: File, loader: ServerLoader): Option[File] = {
    val scriptBits = JavaAppStartScript.generatePostinst(name, loader)
    val script = tmpDir / "tmp" / "bin" / "debian-postinst"
    IO.write(script, scriptBits)
    Some(script)
  }

  protected def makeEtcDefaultScript(name: String, tmpDir: File, loader: ServerLoader, source: java.net.URL): Option[File] = {
    loader match {
      case Upstart => None
      case SystemV => {
        val scriptBits = TemplateWriter.generateScript(source, Seq.empty)
        val script = tmpDir / "tmp" / "etc" / "default" / name
        IO.write(script, scriptBits)
        Some(script)
      }
    }
  }
}
