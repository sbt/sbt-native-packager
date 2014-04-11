package com.typesafe.sbt
package packager
package archetypes

import Keys._
import sbt._
import sbt.Keys.{ target, mainClass, normalizedName, sourceDirectory, streams }
import SbtNativePackager._
import com.typesafe.sbt.packager.linux.{ LinuxFileMetaData, LinuxPackageMapping, LinuxSymlink, LinuxPlugin }
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin

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

  def settings: Seq[Setting[_]] = JavaAppPackaging.settings ++ linuxSettings ++ debianSettings ++ rpmSettings
  protected def etcDefaultTemplateSource: java.net.URL = getClass.getResource("etc-default-template")

  /**
   * general settings which apply to all linux server archetypes
   *
   * - script replacements
   * - logging directory
   * - config directory
   */
  def linuxSettings: Seq[Setting[_]] = Seq(
    // === logging directory mapping ===
    linuxPackageMappings <+= (normalizedName, defaultLinuxLogsLocation, daemonUser in Linux, daemonGroup in Linux) map {
      (name, logsDir, user, group) => packageTemplateMapping(logsDir + "/" + name)() withUser user withGroup group withPerms "755"
    },
    linuxPackageSymlinks <+= (normalizedName, defaultLinuxInstallLocation, defaultLinuxLogsLocation) map {
      (name, install, logsDir) => LinuxSymlink(install + "/" + name + "/logs", logsDir + "/" + name)
    },
    // === etc config mapping ===
    bashScriptConfigLocation <<= normalizedName map (name => Some("/etc/default/" + name)),
    linuxEtcDefaultTemplate <<= sourceDirectory map { dir =>
      val overrideScript = dir / "templates" / "etc-default"
      if (overrideScript.exists) overrideScript.toURI.toURL
      else etcDefaultTemplateSource
    },
    makeEtcDefault <<= (normalizedName, target in Universal, linuxEtcDefaultTemplate, linuxScriptReplacements)
      map makeEtcDefaultScript,
    linuxPackageMappings <++= (makeEtcDefault, normalizedName) map { (conf, name) =>
      conf.map(c => LinuxPackageMapping(Seq(c -> ("/etc/default/" + name)),
        LinuxFileMetaData(Users.Root, Users.Root)).withConfig()).toSeq
    },

    // === /var/run/app pid folder ===
    linuxPackageMappings <+= (normalizedName, daemonUser in Linux, daemonGroup in Linux) map { (name, user, group) =>
      packageTemplateMapping("/var/run/" + name)() withUser user withGroup group withPerms "755"
    })

  def debianSettings: Seq[Setting[_]] =
    Seq(
      linuxJavaAppStartScriptBuilder in Debian := JavaAppStartScript.Debian,
      serverLoading := Upstart,

      // === Startscript creation ===
      linuxStartScriptTemplate in Debian <<= (serverLoading in Debian, sourceDirectory, linuxJavaAppStartScriptBuilder in Debian) map {
        (loader, dir, builder) => builder.defaultStartScriptTemplate(loader, dir / "templates" / "start")
      },
      linuxMakeStartScript in Debian <<= (target in Universal, serverLoading in Debian, linuxScriptReplacements, linuxStartScriptTemplate in Debian, linuxJavaAppStartScriptBuilder in Debian)
        map { (tmpDir, loader, replacements, template, builder) =>
          makeMaintainerScript(builder.startScript, Some(template))(tmpDir, loader, replacements, builder)
        },
      linuxPackageMappings in Debian <++= (normalizedName, linuxMakeStartScript in Debian, serverLoading in Debian) map startScriptMapping,

      // === Maintainer scripts === 
      debianMakePreinstScript <<= (target in Universal, serverLoading in Debian, linuxScriptReplacements, linuxJavaAppStartScriptBuilder in Debian) map makeMaintainerScript(Preinst),
      debianMakePostinstScript <<= (target in Universal, serverLoading in Debian, linuxScriptReplacements, linuxJavaAppStartScriptBuilder in Debian) map makeMaintainerScript(Postinst),
      debianMakePrermScript <<= (target in Universal, serverLoading in Debian, linuxScriptReplacements, linuxJavaAppStartScriptBuilder in Debian) map makeMaintainerScript(Prerm),
      debianMakePostrmScript <<= (target in Universal, serverLoading in Debian, linuxScriptReplacements, linuxJavaAppStartScriptBuilder in Debian) map makeMaintainerScript(Postrm))

  def rpmSettings: Seq[Setting[_]] = Seq(
    linuxJavaAppStartScriptBuilder in Rpm := JavaAppStartScript.Rpm,
    serverLoading in Rpm := SystemV,

    // === Startscript creation ===
    linuxStartScriptTemplate in Rpm <<= (serverLoading in Rpm, sourceDirectory, linuxJavaAppStartScriptBuilder in Rpm) map {
      (loader, dir, builder) =>
        builder.defaultStartScriptTemplate(loader, dir / "templates" / "start")
    },
    linuxMakeStartScript in Rpm <<= (target in Universal, serverLoading in Rpm, linuxScriptReplacements, linuxStartScriptTemplate in Rpm, linuxJavaAppStartScriptBuilder in Rpm)
      map { (tmpDir, loader, replacements, template, builder) =>
        makeMaintainerScript(builder.startScript, Some(template))(tmpDir, loader, replacements, builder)
      },
    linuxPackageMappings in Rpm <++= (normalizedName, linuxMakeStartScript in Rpm, serverLoading in Rpm) map startScriptMapping,

    // == Maintainer scripts ===
    // TODO this is very basic - align debian and rpm plugin
    rpmPre <<= (rpmPre, linuxScriptReplacements) apply { (pre, replacements) =>
      val scriptBits = TemplateWriter.generateScript(RpmPlugin.preinstTemplateSource, replacements)
      Some(pre.map(_ + "\n").getOrElse("") + scriptBits)
    },
    rpmPost <<= (rpmPost, linuxScriptReplacements) apply { (pre, replacements) =>
      val scriptBits = TemplateWriter.generateScript(RpmPlugin.postinstTemplateSource, replacements)
      Some(pre.map(_ + "\n").getOrElse("") + scriptBits)
    },
    rpmPostun <<= (rpmPostun, linuxScriptReplacements) apply { (post, replacements) =>
      val scriptBits = TemplateWriter.generateScript(RpmPlugin.postuninstallTemplateSource, replacements)
      Some(post.map(_ + "\n").getOrElse("") + scriptBits)
    },
    rpmPreun <<= (rpmPostun, linuxScriptReplacements) apply { (post, replacements) =>
      val scriptBits = TemplateWriter.generateScript(RpmPlugin.preuninstallTemplateSource, replacements)
      Some(post.map(_ + "\n").getOrElse("") + scriptBits)
    }
  )

  /* ==========================================  */
  /* ============ Helper Methods ==============  */
  /* ==========================================  */

  protected def startScriptMapping(name: String, script: Option[File], loader: ServerLoader): Seq[LinuxPackageMapping] = {
    val (path, permissions) = loader match {
      case Upstart => ("/etc/init/" + name + ".conf", "0644")
      case SystemV => ("/etc/init.d/" + name, "0755")
    }
    for {
      s <- script.toSeq
    } yield LinuxPackageMapping(Seq(s -> path), LinuxFileMetaData(Users.Root, Users.Root, permissions, "true"))
  }

  protected def makeMaintainerScript(scriptName: String, template: Option[URL] = None)(
    tmpDir: File, loader: ServerLoader, replacements: Seq[(String, String)], builder: JavaAppStartScriptBuilder): Option[File] = {
    builder.generateTemplate(scriptName, loader, replacements, template) map { scriptBits =>
      val script = tmpDir / "tmp" / "bin" / (builder.name + scriptName)
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
