package com.typesafe.sbt
package packager
package archetypes

import Keys._
import sbt._
import sbt.Keys.{ target, mainClass, sourceDirectory, streams }
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

  val CONTROL_FUNCTIONS = "control-functions"

  /** These settings will be provided by this archetype*/
  def settings: Seq[Setting[_]] = JavaAppPackaging.settings ++ linuxSettings ++ debianSettings ++ rpmSettings

  protected def etcDefaultTemplateSource: java.net.URL = getClass.getResource("etc-default-template")
  protected def controlScriptSource: java.net.URL = getClass.getResource(CONTROL_FUNCTIONS)

  /**
   * general settings which apply to all linux server archetypes
   *
   * - script replacements
   * - logging directory
   * - config directory
   */
  def linuxSettings: Seq[Setting[_]] = Seq(
    // === logging directory mapping ===
    linuxPackageMappings <+= (packageName in Linux, defaultLinuxLogsLocation, daemonUser in Linux, daemonGroup in Linux) map {
      (name, logsDir, user, group) => packageTemplateMapping(logsDir + "/" + name)() withUser user withGroup group withPerms "755"
    },
    linuxPackageSymlinks <+= (packageName in Linux, defaultLinuxInstallLocation, defaultLinuxLogsLocation) map {
      (name, install, logsDir) => LinuxSymlink(install + "/" + name + "/logs", logsDir + "/" + name)
    },
    // === etc config mapping ===
    bashScriptConfigLocation <<= (packageName in Linux) map (name => Some("/etc/default/" + name)),
    linuxEtcDefaultTemplate <<= sourceDirectory map { dir =>
      val overrideScript = dir / "templates" / "etc-default"
      if (overrideScript.exists) overrideScript.toURI.toURL
      else etcDefaultTemplateSource
    },
    makeEtcDefault <<= (packageName in Linux, target in Universal, linuxEtcDefaultTemplate, linuxScriptReplacements)
      map makeEtcDefaultScript,
    linuxPackageMappings <++= (makeEtcDefault, packageName in Linux) map { (conf, name) =>
      conf.map(c => LinuxPackageMapping(Seq(c -> ("/etc/default/" + name)),
        LinuxFileMetaData(Users.Root, Users.Root, "644")).withConfig()).toSeq
    },

    // === /var/run/app pid folder ===
    linuxPackageMappings <+= (packageName in Linux, daemonUser in Linux, daemonGroup in Linux) map { (name, user, group) =>
      packageTemplateMapping("/var/run/" + name)() withUser user withGroup group withPerms "755"
    },

    linuxScriptReplacements += controlScriptFunctionsReplacement( /* Add key for control-functions */ )
  )

  def debianSettings: Seq[Setting[_]] = {
    import DebianPlugin.Names.{ Preinst, Postinst, Prerm, Postrm }
    inConfig(Debian)(Seq(
      serverLoading := Upstart,
      startRunlevels <<= (serverLoading) apply defaultStartRunlevels,
      stopRunlevels <<= (serverLoading) apply defaultStopRunlevels,
      requiredStartFacilities <<= (serverLoading) apply defaultFacilities,
      requiredStopFacilities <<= (serverLoading) apply defaultFacilities,
      // === Startscript creation ===
      linuxScriptReplacements <++= (requiredStartFacilities, requiredStopFacilities, startRunlevels, stopRunlevels, serverLoading) apply
        makeStartScriptReplacements,
      linuxScriptReplacements += ("header" -> "#!/bin/sh\n"),
      linuxScriptReplacements += BashScript.loaderFunctionsReplacement(serverLoading.value, "java_server"),

      linuxStartScriptTemplate := JavaAppStartScript(
	name = startScriptName(serverLoading.value, Debian),
	loader = serverLoading.value,
	archetype = "java_server",
	template = Some(sourceDirectory.value / "templates" / "start")
      ),
      //      linuxStartScriptTemplate <<= (serverLoading, sourceDirectory, linuxBashScriptBuilder) map {
      //        (loader, dir, builder) => builder.startScriptURL(loader, Some(dir / "templates" / "start"), "java_app")
      //      },
      defaultLinuxStartScriptLocation <<= serverLoading apply getStartScriptLocation,
      linuxMakeStartScript <<= (target in Universal, serverLoading, linuxScriptReplacements, linuxStartScriptTemplate)
	map { (tmpDir, loader, replacements, template) =>
	  makeMaintainerScript("start", Some(template))(tmpDir, loader, replacements)
        },
      linuxPackageMappings <++= (packageName, linuxMakeStartScript, serverLoading, defaultLinuxStartScriptLocation) map startScriptMapping
    )) ++ Seq(
      // === Maintainer scripts === 
      debianMakePreinstScript <<= (target in Universal, serverLoading in Debian, linuxScriptReplacements) map makeMaintainerScript(Preinst),
      debianMakePostinstScript <<= (target in Universal, serverLoading in Debian, linuxScriptReplacements) map makeMaintainerScript(Postinst),
      debianMakePrermScript <<= (target in Universal, serverLoading in Debian, linuxScriptReplacements) map makeMaintainerScript(Prerm),
      debianMakePostrmScript <<= (target in Universal, serverLoading in Debian, linuxScriptReplacements) map makeMaintainerScript(Postrm))
  }

  def rpmSettings: Seq[Setting[_]] = {
    import RpmPlugin.Names.{ Pre, Post, Preun, Postun }
    inConfig(Rpm)(Seq(
      serverLoading := SystemV,
      startRunlevels <<= (serverLoading) apply defaultStartRunlevels,
      stopRunlevels in Rpm <<= (serverLoading) apply defaultStopRunlevels,
      requiredStartFacilities in Rpm <<= (serverLoading) apply defaultFacilities,
      requiredStopFacilities in Rpm <<= (serverLoading) apply defaultFacilities,
      linuxScriptReplacements <++= (requiredStartFacilities, requiredStopFacilities, startRunlevels, stopRunlevels, serverLoading) apply
	makeStartScriptReplacements,
      linuxScriptReplacements += BashScript.loaderFunctionsReplacement(serverLoading.value, "java_server")
    )) ++ Seq(
      // === Startscript creation ===
      linuxStartScriptTemplate := JavaAppStartScript(
	name = startScriptName((serverLoading in Rpm).value, Rpm),
	loader = (serverLoading in Rpm).value,
	archetype = "java_server",
	template = Some(sourceDirectory.value / "templates" / "start")
      ),
      linuxMakeStartScript in Rpm <<= (target in Universal, serverLoading in Rpm, linuxScriptReplacements in Rpm, linuxStartScriptTemplate in Rpm)
	map { (tmpDir, loader, replacements, template) =>
	  makeMaintainerScript("start", Some(template))(tmpDir, loader, replacements)
        },
      defaultLinuxStartScriptLocation in Rpm <<= (serverLoading in Rpm) apply getStartScriptLocation,
      linuxPackageMappings in Rpm <++= (packageName in Rpm, linuxMakeStartScript in Rpm, serverLoading in Rpm, defaultLinuxStartScriptLocation in Rpm) map startScriptMapping,

      // == Maintainer scripts ===
      // TODO this is very basic - align debian and rpm plugin
      rpmPre <<= (rpmScriptsDirectory, rpmPre, linuxScriptReplacements, serverLoading in Rpm) apply {
	(dir, pre, replacements, loader) =>
	  Some(pre.map(_ + "\n").getOrElse("") + rpmScriptletContent(dir, Pre, replacements))
      },
      rpmPost <<= (rpmScriptsDirectory, rpmPost, linuxScriptReplacements, serverLoading in Rpm) apply {
	(dir, post, replacements, loader) =>
	  Some(post.map(_ + "\n").getOrElse("") + rpmScriptletContent(dir, Post, replacements))
      },
      rpmPostun <<= (rpmScriptsDirectory, rpmPostun, linuxScriptReplacements, serverLoading in Rpm) apply {
	(dir, postun, replacements, loader) =>
	  Some(postun.map(_ + "\n").getOrElse("") + rpmScriptletContent(dir, Postun, replacements))
      },
      rpmPreun <<= (rpmScriptsDirectory, rpmPreun, linuxScriptReplacements, serverLoading in Rpm) apply {
	(dir, preun, replacements, loader) =>
	  Some(preun.map(_ + "\n").getOrElse("") + rpmScriptletContent(dir, Preun, replacements))
      }
    )
  }

  /* ==========================================  */
  /* ============ Helper Methods ==============  */
  /* ==========================================  */

  private[this] def startScriptName(loader: ServerLoader, config: Configuration): String = (loader, config.name) match {
    // SystemV has two different start scripts
    case (SystemV, name) => s"start-$name-template"
    case _               => "start-template"
  }

  private[this] def makeStartScriptReplacements(
    requiredStartFacilities: String,
    requiredStopFacilities: String,
    startRunlevels: String,
    stopRunlevels: String,
    loader: ServerLoader): Seq[(String, String)] = {
    loader match {
      case SystemV =>
	Seq("start_runlevels" -> startRunlevels,
	  "stop_runlevels" -> stopRunlevels,
	  "start_facilities" -> requiredStartFacilities,
	  "stop_facilities" -> requiredStopFacilities)
      case Upstart =>
	Seq("start_runlevels" -> startRunlevels,
	  "stop_runlevels" -> stopRunlevels,
	  "start_facilities" -> requiredStartFacilities,
	  "stop_facilities" -> requiredStopFacilities)
      case Systemd =>
	Seq("start_facilities" -> requiredStartFacilities)
    }
  }

  private[this] def defaultFacilities(loader: ServerLoader): String = {
    loader match {
      case SystemV => "$remote_fs $syslog"
      case Upstart => "[networking]"
      case Systemd => "network.target"
    }
  }

  private[this] def defaultStartRunlevels(loader: ServerLoader): String = {
    loader match {
      case SystemV => "2 3 4 5"
      case Upstart => "[2345]"
      case Systemd => ""
    }
  }

  private[this] def defaultStopRunlevels(loader: ServerLoader): String = {
    loader match {
      case SystemV => "0 1 6"
      case Upstart => "[016]"
      case Systemd => ""
    }
  }

  private[this] def getStartScriptLocation(loader: ServerLoader): String = {
    loader match {
      case Upstart => "/etc/init/"
      case SystemV => "/etc/init.d/"
      case Systemd => "/usr/lib/systemd/system/"
    }
  }

  /**
   * Load the default controlscript functions which contain
   * addUser/removeUser/addGroup/removeGroup
   *
   * @return placeholder->content
   */
  protected def controlScriptFunctionsReplacement(template: Option[URL] = None): (String, String) = {
    val url = template getOrElse controlScriptSource
    CONTROL_FUNCTIONS -> TemplateWriter.generateScript(source = url, replacements = Nil)
  }

  protected def startScriptMapping(name: String, script: Option[File], loader: ServerLoader, scriptDir: String): Seq[LinuxPackageMapping] = {
    val (path, permissions) = loader match {
      case Upstart => ("/etc/init/" + name + ".conf", "0644")
      case SystemV => ("/etc/init.d/" + name, "0755")
      case Systemd => ("/usr/lib/systemd/system/" + name + ".service", "0644")
    }
    for {
      s <- script.toSeq
    } yield LinuxPackageMapping(Seq(s -> path), LinuxFileMetaData(Users.Root, Users.Root, permissions, "true"))
  }

  protected def makeMaintainerScript(scriptName: String,
    template: Option[URL] = None, archetype: String = "java_server", config: Configuration = Debian)(
      tmpDir: File, loader: ServerLoader, replacements: Seq[(String, String)]): Option[File] = {
    val scriptBits = BashScript(scriptName, archetype, config, replacements, template)
    val script = tmpDir / "tmp" / "bin" / (config.name + scriptName)
    IO.write(script, scriptBits)
    Some(script)
  }

  protected def makeEtcDefaultScript(name: String, tmpDir: File, source: java.net.URL, replacements: Seq[(String, String)]): Option[File] = {
    val scriptBits = TemplateWriter.generateScript(source, replacements)
    val script = tmpDir / "tmp" / "etc" / "default" / name
    IO.write(script, scriptBits)
    Some(script)
  }

  protected def rpmScriptletContent(dir: File, script: String,
    replacements: Seq[(String, String)], archetype: String = "java_server", config: Configuration = Rpm): String = {
    val file = (dir / script)
    val template = if (file exists) Some(file.toURI.toURL) else None
    BashScript(script, archetype, config, replacements, template)
  }

}
