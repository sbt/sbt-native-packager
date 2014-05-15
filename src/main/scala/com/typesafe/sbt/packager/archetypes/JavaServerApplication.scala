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

  def settings: Seq[Setting[_]] = JavaAppPackaging.settings ++ linuxSettings ++ debianSettings ++ rpmSettings
  protected def etcDefaultTemplateSource: java.net.URL = getClass.getResource("etc-default-template")

  private[this] def makeStartScriptReplacements(
    requiredStartFacilities: String,
    requiredStopFacilities: String,
    startRunlevels: String,
    stopRunlevels: String,
    loader: ServerLoader.ServerLoader): Seq[(String, String)] = {
    loader match {
      case ServerLoader.SystemV =>
        Seq("start_runlevels" -> startRunlevels,
          "stop_runlevels" -> stopRunlevels,
          "start_facilities" -> requiredStartFacilities,
          "stop_facilities" -> requiredStopFacilities)
      case ServerLoader.Upstart =>
        Seq("start_runlevels" -> startRunlevels,
          "stop_runlevels" -> stopRunlevels,
          "start_facilities" -> requiredStartFacilities,
          "stop_facilities" -> requiredStopFacilities)
    }
  }

  private[this] def defaultFacilities(loader: ServerLoader.ServerLoader): String = {
    loader match {
      case ServerLoader.SystemV => "$remote_fs $syslog"
      case ServerLoader.Upstart => "networking"
    }
  }

  private[this] def defaultStartRunlevels(loader: ServerLoader.ServerLoader): String = {
    loader match {
      case ServerLoader.SystemV => "2 3 4 5"
      case ServerLoader.Upstart => "2345"
    }
  }

  private[this] def defaultStopRunlevels(loader: ServerLoader.ServerLoader): String = {
    loader match {
      case ServerLoader.SystemV => "0 1 6"
      case ServerLoader.Upstart => "016"
    }
  }

  /**
   * general settings which apply to all linux server archetypes
   *
   * - script replacements
   * - logging directory
   * - config directory
   */
  def linuxSettings: Seq[Setting[_]] = Seq(
    // === logging directory mapping ===
    linuxPackageMappings <+= (normalizedName in Linux, defaultLinuxLogsLocation, daemonUser in Linux, daemonGroup in Linux) map {
      (name, logsDir, user, group) => packageTemplateMapping(logsDir + "/" + name)() withUser user withGroup group withPerms "755"
    },
    linuxPackageSymlinks <+= (normalizedName in Linux, defaultLinuxInstallLocation, defaultLinuxLogsLocation) map {
      (name, install, logsDir) => LinuxSymlink(install + "/" + name + "/logs", logsDir + "/" + name)
    },
    // === etc config mapping ===
    bashScriptConfigLocation <<= (normalizedName in Linux) map (name => Some("/etc/default/" + name)),
    linuxEtcDefaultTemplate <<= sourceDirectory map { dir =>
      val overrideScript = dir / "templates" / "etc-default"
      if (overrideScript.exists) overrideScript.toURI.toURL
      else etcDefaultTemplateSource
    },
    makeEtcDefault <<= (normalizedName in Linux, target in Universal, linuxEtcDefaultTemplate, linuxScriptReplacements)
      map makeEtcDefaultScript,
    linuxPackageMappings <++= (makeEtcDefault, normalizedName) map { (conf, name) =>
      conf.map(c => LinuxPackageMapping(Seq(c -> ("/etc/default/" + name)),
        LinuxFileMetaData(Users.Root, Users.Root)).withConfig()).toSeq
    },

    // === /var/run/app pid folder ===
    linuxPackageMappings <+= (normalizedName in Linux, daemonUser in Linux, daemonGroup in Linux) map { (name, user, group) =>
      packageTemplateMapping("/var/run/" + name)() withUser user withGroup group withPerms "755"
    })

  def debianSettings: Seq[Setting[_]] = {
    import DebianPlugin.Names.{ Preinst, Postinst, Prerm, Postrm }
    Seq(
      serverLoading in Debian := Upstart,
      startRunlevels in Debian <<= (serverLoading in Debian) apply defaultStartRunlevels,
      stopRunlevels in Debian <<= (serverLoading in Debian) apply defaultStopRunlevels,
      requiredStartFacilities in Debian <<= (serverLoading in Debian) apply defaultFacilities,
      requiredStopFacilities in Debian <<= (serverLoading in Debian) apply defaultFacilities,
      linuxJavaAppStartScriptBuilder in Debian := JavaAppStartScript.Debian,
      // === Startscript creation ===
      linuxScriptReplacements in Debian <++= (requiredStartFacilities in Debian, requiredStopFacilities in Debian, startRunlevels in Debian, stopRunlevels in Debian, serverLoading in Debian) apply
        makeStartScriptReplacements,
      linuxStartScriptTemplate in Debian <<= (serverLoading in Debian, sourceDirectory, linuxJavaAppStartScriptBuilder in Debian) map {
        (loader, dir, builder) => builder.defaultStartScriptTemplate(loader, dir / "templates" / "start")
      },
      linuxMakeStartScript in Debian <<= (target in Universal, serverLoading in Debian, linuxScriptReplacements in Debian, linuxStartScriptTemplate in Debian, linuxJavaAppStartScriptBuilder in Debian)
        map { (tmpDir, loader, replacements, template, builder) =>
          println(replacements)
          makeMaintainerScript(builder.startScript, Some(template))(tmpDir, loader, replacements, builder)
        },
      linuxPackageMappings in Debian <++= (normalizedName in Linux, linuxMakeStartScript in Debian, serverLoading in Debian) map startScriptMapping,

      // === Maintainer scripts === 
      debianMakePreinstScript <<= (target in Universal, serverLoading in Debian, linuxScriptReplacements, linuxJavaAppStartScriptBuilder in Debian) map makeMaintainerScript(Preinst),
      debianMakePostinstScript <<= (target in Universal, serverLoading in Debian, linuxScriptReplacements, linuxJavaAppStartScriptBuilder in Debian) map makeMaintainerScript(Postinst),
      debianMakePrermScript <<= (target in Universal, serverLoading in Debian, linuxScriptReplacements, linuxJavaAppStartScriptBuilder in Debian) map makeMaintainerScript(Prerm),
      debianMakePostrmScript <<= (target in Universal, serverLoading in Debian, linuxScriptReplacements, linuxJavaAppStartScriptBuilder in Debian) map makeMaintainerScript(Postrm))
  }

  def rpmSettings: Seq[Setting[_]] = {
    import RpmPlugin.Names.{ Pre, Post, Preun, Postun }
    Seq(
      serverLoading in Rpm := SystemV,
      startRunlevels in Rpm <<= (serverLoading in Debian) apply defaultStartRunlevels,
      stopRunlevels in Rpm <<= (serverLoading in Debian) apply defaultStopRunlevels,
      requiredStartFacilities in Rpm <<= (serverLoading in Rpm) apply defaultFacilities,
      requiredStopFacilities in Rpm <<= (serverLoading in Rpm) apply defaultFacilities,
      linuxJavaAppStartScriptBuilder in Rpm := JavaAppStartScript.Rpm,
      linuxScriptReplacements in Rpm <++= (requiredStartFacilities in Rpm, requiredStopFacilities in Rpm, startRunlevels in Rpm, stopRunlevels in Rpm, serverLoading in Rpm) apply
        makeStartScriptReplacements,
      // === Startscript creation ===
      linuxStartScriptTemplate in Rpm <<= (serverLoading in Rpm, sourceDirectory, linuxJavaAppStartScriptBuilder in Rpm) map {
        (loader, dir, builder) =>
          builder.defaultStartScriptTemplate(loader, dir / "templates" / "start")
      },
      linuxMakeStartScript in Rpm <<= (target in Universal, serverLoading in Rpm, linuxScriptReplacements, linuxStartScriptTemplate in Rpm, linuxJavaAppStartScriptBuilder in Rpm)
        map { (tmpDir, loader, replacements, template, builder) =>
          makeMaintainerScript(builder.startScript, Some(template))(tmpDir, loader, replacements, builder)
        },
      linuxPackageMappings in Rpm <++= (normalizedName in Linux, linuxMakeStartScript in Rpm, serverLoading in Rpm) map startScriptMapping,

      // == Maintainer scripts ===
      // TODO this is very basic - align debian and rpm plugin
      rpmPre <<= (rpmScriptsDirectory, rpmPre, linuxScriptReplacements, serverLoading in Rpm, linuxJavaAppStartScriptBuilder in Rpm) apply {
        (dir, pre, replacements, loader, builder) =>
          Some(pre.map(_ + "\n").getOrElse("") + rpmScriptletContent(dir, Pre, loader, replacements, builder))
      },
      rpmPost <<= (rpmScriptsDirectory, rpmPost, linuxScriptReplacements, serverLoading in Rpm, linuxJavaAppStartScriptBuilder in Rpm) apply {
        (dir, post, replacements, loader, builder) =>
          Some(post.map(_ + "\n").getOrElse("") + rpmScriptletContent(dir, Post, loader, replacements, builder))
      },
      rpmPostun <<= (rpmScriptsDirectory, rpmPostun, linuxScriptReplacements, serverLoading in Rpm, linuxJavaAppStartScriptBuilder in Rpm) apply {
        (dir, postun, replacements, loader, builder) =>
          Some(postun.map(_ + "\n").getOrElse("") + rpmScriptletContent(dir, Postun, loader, replacements, builder))
      },
      rpmPreun <<= (rpmScriptsDirectory, rpmPreun, linuxScriptReplacements, serverLoading in Rpm, linuxJavaAppStartScriptBuilder in Rpm) apply {
        (dir, preun, replacements, loader, builder) =>
          Some(preun.map(_ + "\n").getOrElse("") + rpmScriptletContent(dir, Preun, loader, replacements, builder))
      }
    )
  }

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

  protected def rpmScriptletContent(dir: File, script: String,
    loader: ServerLoader, replacements: Seq[(String, String)], builder: JavaAppStartScriptBuilder): String = {
    val file = (dir / script)
    val template = if (file exists) Some(file.toURI.toURL) else None
    builder.generateTemplate(script, loader, replacements, template).getOrElse(sys.error("Could generate content for script: " + script))
  }
}
