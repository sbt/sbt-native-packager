package com.typesafe.sbt
package packager
package archetypes

import Keys._
import sbt._
import sbt.Keys.{ target, mainClass, normalizedName, sourceDirectory }
import SbtNativePackager._
import com.typesafe.sbt.packager.linux.{ LinuxFileMetaData, LinuxPackageMapping, LinuxSymlink, LinuxPlugin }
import com.typesafe.sbt.packager.debian.DebianPlugin

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
  import DebianPlugin.Names.{ Preinst, Postinst, Prerm, Postrm }

  def settings: Seq[Setting[_]] = JavaAppPackaging.settings ++ debianSettings
  protected def etcDefaultTemplateSource: java.net.URL = getClass.getResource("etc-default-template")

  def debianSettings: Seq[Setting[_]] =
    Seq(
      serverLoading := Upstart,
      daemonUser <<= appUser in Linux,
      // This one is begging for sbt 0.13 syntax...
      debianScriptReplacements <<= (
        maintainer in Debian, packageSummary in Debian, serverLoading in Debian, daemonUser in Debian, normalizedName,
        sbt.Keys.version, defaultLinuxInstallLocation)
        map { (author, descr, loader, daemonUser, name, version, installLocation) =>
          val appDir = installLocation + "/" + name

          JavaAppStartScript.makeReplacements(
            author = author,
            description = descr,
            execScript = name,
            chdir = appDir,
            appName = name,
            daemonUser = daemonUser)
        },
      // TODO - Default locations shouldn't be so hacky.

      // === Startscript creation ===
      linuxStartScriptTemplate in Debian <<= (serverLoading in Debian, sourceDirectory) map { (loader, dir) =>
        JavaAppStartScript.defaultStartScriptTemplate(loader, dir / "templates" / "start")
      },
      debianMakeStartScript <<= (target in Universal, serverLoading in Debian, debianScriptReplacements, linuxStartScriptTemplate in Debian)
        map { (tmpDir, loader, replacements, template) =>
          makeDebianMaintainerScript(JavaAppStartScript.startScript, Some(template))(tmpDir, loader, replacements)
        },
      linuxPackageMappings in Debian <++= (debianMakeStartScript, normalizedName, serverLoading in Debian, appUser in Linux)
        map { (script, name, loader, owner) =>
          val (path, permissions) = loader match {
            case Upstart => ("/etc/init/" + name + ".conf", "0644")
            case SystemV => ("/etc/init.d/" + name, "0755")
          }
          for {
            s <- script.toSeq
          } yield LinuxPackageMapping(Seq(s -> path), LinuxFileMetaData(owner, owner, permissions, "true"))
        },

      // === etc config mapping ===
      bashScriptConfigLocation <<= normalizedName map (name => Some("/etc/default/" + name)),
      linuxEtcDefaultTemplate in Debian <<= sourceDirectory map { dir =>
        val overrideScript = dir / "templates" / "etc-default"
        if (overrideScript.exists) overrideScript.toURI.toURL
        else etcDefaultTemplateSource
      },
      debianMakeEtcDefault <<= (normalizedName, target in Universal, linuxEtcDefaultTemplate in Debian, debianScriptReplacements)
        map makeEtcDefaultScript,
      linuxPackageMappings in Debian <++= (debianMakeEtcDefault, normalizedName, appUser in Linux) map { (conf, name, owner) =>
        conf.map(c => LinuxPackageMapping(Seq(c -> ("/etc/default/" + name)), LinuxFileMetaData(owner, owner)).withConfig()).toSeq
      },
      // TODO should we specify daemonGroup in configs?

      // === logging directory mapping ===
      linuxPackageMappings in Debian <+= (normalizedName, defaultLinuxLogsLocation, target in Debian, appUser in Linux) map {
        (name, logsDir, target, user) =>
          // create empty var/log directory
          val d = target / logsDir
          d.mkdirs()
          LinuxPackageMapping(Seq(d -> (logsDir + "/" + name)), LinuxFileMetaData(user, user))
      },
      linuxPackageSymlinks in Debian <+= (normalizedName, defaultLinuxInstallLocation) map {
        (name, install) => LinuxSymlink(install + "/" + name + "/logs", "/var/log/" + name)
      },

      // === Maintainer scripts === 
      debianMakePreinstScript <<= (target in Universal, serverLoading in Debian, debianScriptReplacements) map makeDebianMaintainerScript(Preinst),
      debianMakePostinstScript <<= (target in Universal, serverLoading in Debian, debianScriptReplacements) map makeDebianMaintainerScript(Postinst),
      debianMakePrermScript <<= (target in Universal, serverLoading in Debian, debianScriptReplacements) map makeDebianMaintainerScript(Prerm),
      debianMakePostrmScript <<= (target in Universal, serverLoading in Debian, debianScriptReplacements) map makeDebianMaintainerScript(Postrm))

  protected def makeDebianMaintainerScript(scriptName: String, template: Option[URL] = None)(
    tmpDir: File, loader: ServerLoader, replacements: Seq[(String, String)]): Option[File] = {
    JavaAppStartScript.generateTemplate(scriptName, loader, replacements, template) map { scriptBits =>
      val script = tmpDir / "tmp" / "bin" / ("debian-" + scriptName)
      IO.write(script, scriptBits)
      script
    }
  }

  protected def makeEtcDefaultScript(name: String, tmpDir: File, source: java.net.URL, replacements: Seq[(String, String)]): Option[File] = {
    val scriptBits = TemplateWriter.generateScript(source, replacements)
    val script = tmpDir / "tmp" / "etc" / "default" / name
    IO.write(script, scriptBits)
    Some(script)
  }
}
