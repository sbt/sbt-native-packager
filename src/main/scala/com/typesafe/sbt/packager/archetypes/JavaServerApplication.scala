package com.typesafe.sbt
package packager
package archetypes

import sbt._
import sbt.Keys.{ target, mainClass, sourceDirectory, streams, javaOptions, run }
import SbtNativePackager.{ Debian, Rpm, Universal }
import packager.Keys.{ packageName, maintainerScripts, daemonStdoutLogFile }
import linux.{ LinuxFileMetaData, LinuxPackageMapping, LinuxSymlink, LinuxPlugin }
import linux.LinuxPlugin.autoImport._
import debian.DebianPlugin
import debian.DebianPlugin.autoImport.{ debianMakePreinstScript, debianMakePostinstScript, debianMakePrermScript, debianMakePostrmScript }
import rpm.RpmPlugin
import rpm.RpmPlugin.autoImport.{ rpmPre, rpmPost, rpmPostun, rpmPreun, rpmScriptsDirectory, rpmDaemonLogFile, RpmConstants }
import JavaAppPackaging.autoImport.{ bashScriptConfigLocation, bashScriptEnvConfigLocation }

/**
 * This class contains the default settings for creating and deploying an archetypical Java application.
 *  A Java application archetype is defined as a project that has a main method and is run by placing
 *  all of its JAR files on the classpath and calling that main method.
 *
 *  This doesn't create the best of distributions, but it can simplify the distribution of code.
 *
 *  **NOTE:  EXPERIMENTAL**   This currently only supports debian upstart scripts.
 */
object JavaServerAppPackaging extends AutoPlugin {
  import ServerLoader._
  import LinuxPlugin.Users

  object Names {
    val DaemonStdoutLogFileReplacement = "daemon_log_file"
  }

  override def requires = JavaAppPackaging

  object autoImport extends JavaServerAppKeys

  override def projectSettings = javaServerSettings

  val ARCHETYPE = "java_server"
  val ENV_CONFIG_REPLACEMENT = "env_config"
  val ETC_DEFAULT = "etc-default"

  /** These settings will be provided by this archetype*/
  def javaServerSettings: Seq[Setting[_]] = linuxSettings ++ debianSettings ++ rpmSettings

  /**
   * general settings which apply to all linux server archetypes
   *
   * - script replacements
   * - logging directory
   * - config directory
   */
  def linuxSettings: Seq[Setting[_]] = Seq(
    javaOptions in Linux <<= javaOptions in Universal,
    // === logging directory mapping ===
    linuxPackageMappings <+= (packageName in Linux, defaultLinuxLogsLocation, daemonUser in Linux, daemonGroup in Linux) map {
      (name, logsDir, user, group) => packageTemplateMapping(logsDir + "/" + name)() withUser user withGroup group withPerms "755"
    },
    linuxPackageSymlinks <+= (packageName in Linux, defaultLinuxInstallLocation, defaultLinuxLogsLocation) map {
      (name, install, logsDir) => LinuxSymlink(install + "/" + name + "/logs", logsDir + "/" + name)
    },
    // === etc config mapping ===
    bashScriptEnvConfigLocation := Some("/etc/default/" + (packageName in Linux).value),

    linuxStartScriptName := None,

    daemonStdoutLogFile := None
  )

  /* etcDefaultConfig is dependent on serverLoading (systemd, systemv, etc.),
   * and is therefore distro specific. As such, these settings cannot be defined
   * in the global config scope. */
  private[this] val etcDefaultConfig: Seq[Setting[_]] = Seq(
    linuxEtcDefaultTemplate := getEtcTemplateSource(
      sourceDirectory.value,
      serverLoading.value),
    makeEtcDefault := makeEtcDefaultScript(
      packageName.value,
      (target in Universal).value,
      linuxEtcDefaultTemplate.value,
      linuxScriptReplacements.value),
    linuxPackageMappings ++= etcDefaultMapping(
      makeEtcDefault.value,
      bashScriptEnvConfigLocation.value)
  )

  def debianSettings: Seq[Setting[_]] = {
    import DebianPlugin.Names.{ Preinst, Postinst, Prerm, Postrm }
    inConfig(Debian)(etcDefaultConfig) ++
    inConfig(Debian)(Seq(
      serverLoading := Upstart,
      startRunlevels <<= (serverLoading) apply defaultStartRunlevels,
      stopRunlevels <<= (serverLoading) apply defaultStopRunlevels,
      requiredStartFacilities <<= (serverLoading) apply defaultFacilities,
      requiredStopFacilities <<= (serverLoading) apply defaultFacilities,
      // === Startscript creation ===
      linuxScriptReplacements <++= (requiredStartFacilities, requiredStopFacilities, startRunlevels, stopRunlevels, serverLoading) apply
        makeStartScriptReplacements,
      linuxScriptReplacements += JavaServerLoaderScript.loaderFunctionsReplacement(serverLoading.value, ARCHETYPE),
      linuxScriptReplacements ++= bashScriptEnvConfigLocation.value.map(ENV_CONFIG_REPLACEMENT -> _).toSeq,
      linuxScriptReplacements += Names.DaemonStdoutLogFileReplacement -> daemonStdoutLogFile.value.getOrElse(""),

      linuxStartScriptTemplate <<= (serverLoading in Debian, sourceDirectory) map { (loader, srcDir) =>
        JavaServerLoaderScript(
          script = defaultTemplateName(loader, Debian),
          loader = loader,
          archetype = ARCHETYPE,
          template = overrideTemplate(srcDir, loader, Debian)
        )
      },
      defaultLinuxStartScriptLocation <<= serverLoading apply getStartScriptLocation,
      linuxMakeStartScript in Debian <<= (linuxStartScriptTemplate in Debian,
        linuxScriptReplacements in Debian,
        target in Universal,
        serverLoading in Debian) map makeStartScript,
      linuxPackageMappings <++= (packageName, linuxMakeStartScript, serverLoading, defaultLinuxStartScriptLocation, linuxStartScriptName) map startScriptMapping,

      // === Maintainer scripts ===
      maintainerScripts := {
        val scripts = (maintainerScripts in Debian).value
        val replacements = (linuxScriptReplacements in Debian).value
        val contentOf = getScriptContent(Debian, replacements) _

        scripts ++ Map(
          Preinst -> (scripts.getOrElse(Preinst, Nil) :+ contentOf(Preinst)),
          Postinst -> (scripts.getOrElse(Postinst, Nil) :+ contentOf(Postinst)),
          Prerm -> (scripts.getOrElse(Prerm, Nil) :+ contentOf(Prerm)),
          Postrm -> (scripts.getOrElse(Postrm, Nil) :+ contentOf(Postrm))
        )
      }
    )) ++ Seq(
      // === Daemon User and Group ===
      daemonUser in Debian <<= daemonUser in Linux,
      daemonUserUid in Debian <<= daemonUserUid in Linux,
      daemonGroup in Debian <<= daemonGroup in Linux,
      daemonGroupGid in Debian <<= daemonGroupGid in Linux
    )
  }

  def rpmSettings: Seq[Setting[_]] = {
    import RpmPlugin.Names.{ Pre, Post, Preun, Postun }
    inConfig(Rpm)(etcDefaultConfig) ++
    inConfig(Rpm)(Seq(
      serverLoading := SystemV,
      startRunlevels <<= (serverLoading) apply defaultStartRunlevels,
      stopRunlevels in Rpm <<= (serverLoading) apply defaultStopRunlevels,
      requiredStartFacilities in Rpm <<= (serverLoading) apply defaultFacilities,
      requiredStopFacilities in Rpm <<= (serverLoading) apply defaultFacilities,
      linuxScriptReplacements <++= (requiredStartFacilities, requiredStopFacilities, startRunlevels, stopRunlevels, serverLoading) apply
        makeStartScriptReplacements,
      linuxScriptReplacements += JavaServerLoaderScript.loaderFunctionsReplacement(serverLoading.value, ARCHETYPE),
      linuxScriptReplacements ++= bashScriptEnvConfigLocation.value.map(ENV_CONFIG_REPLACEMENT -> _).toSeq,
      linuxScriptReplacements += Names.DaemonStdoutLogFileReplacement -> daemonStdoutLogFile.value.getOrElse(""),

      // === /var/run/app pid folder ===
      linuxPackageMappings <+= (packageName, daemonUser, daemonGroup) map { (name, user, group) =>
        packageTemplateMapping("/var/run/" + name)() withUser user withGroup group withPerms "755"
      }
    )) ++ Seq(
      // === Daemon User and Group ===
      daemonUser in Rpm <<= daemonUser in Linux,
      daemonUserUid in Rpm <<= daemonUserUid in Linux,
      daemonGroup in Rpm <<= daemonGroup in Linux,
      daemonGroupGid in Rpm <<= daemonGroupGid in Linux,
      // === Startscript creation ===
      linuxStartScriptTemplate <<= (serverLoading in Rpm, sourceDirectory) map { (loader, srcDir) =>
        JavaServerLoaderScript(
          script = defaultTemplateName(loader, Rpm),
          loader = loader,
          archetype = ARCHETYPE,
          template = overrideTemplate(srcDir, loader, Rpm)
        )
      },
      linuxMakeStartScript in Rpm <<= (linuxStartScriptTemplate in Rpm,
        linuxScriptReplacements in Rpm,
        target in Universal,
        serverLoading in Rpm) map makeStartScript,

      defaultLinuxStartScriptLocation in Rpm <<= (serverLoading in Rpm) apply getStartScriptLocation,
      linuxStartScriptName in Rpm <<= linuxStartScriptName in Linux,
      linuxPackageMappings in Rpm <++= (packageName in Rpm, linuxMakeStartScript in Rpm, serverLoading in Rpm, defaultLinuxStartScriptLocation in Rpm, linuxStartScriptName in Rpm) map startScriptMapping,

      // == Maintainer scripts ===
      maintainerScripts in Rpm := rpmScriptletContents(rpmScriptsDirectory.value, (maintainerScripts in Rpm).value, (linuxScriptReplacements in Rpm).value)
    )
  }

  /* ==========================================  */
  /* ============ Helper Methods ==============  */
  /* ==========================================  */

  private[this] def defaultTemplateName(loader: ServerLoader, config: Configuration): String = (loader, config.name) match {
    // SystemV has two different start scripts
    case (SystemV, name) => s"start-$name-template"
    case _ => "start-template"
  }

  private[this] def overrideTemplate(sourceDirectory: File, loader: ServerLoader, config: Configuration): Option[File] = {
    Option(sourceDirectory / "templates" / config.name / loader.toString.toLowerCase)
  }

  private[this] def makeStartScriptReplacements(
    requiredStartFacilities: Option[String],
    requiredStopFacilities: Option[String],
    startRunlevels: Option[String],
    stopRunlevels: Option[String],
    loader: ServerLoader): Seq[(String, String)] = {

    // Upstart cannot handle empty values
    val (startOn, stopOn) = loader match {
      case Upstart => (requiredStartFacilities.map("start on started " + _), requiredStopFacilities.map("stop on stopping " + _))
      case _ => (requiredStartFacilities, requiredStopFacilities)
    }
    Seq(
      "start_runlevels" -> startRunlevels.getOrElse(""),
      "stop_runlevels" -> stopRunlevels.getOrElse(""),
      "start_facilities" -> startOn.getOrElse(""),
      "stop_facilities" -> stopOn.getOrElse("")
    )
  }

  private[this] def defaultFacilities(loader: ServerLoader): Option[String] = {
    Option(loader match {
      case SystemV => "$remote_fs $syslog"
      case Upstart => null
      case Systemd => "network.target"
    })
  }

  private[this] def defaultStartRunlevels(loader: ServerLoader): Option[String] = {
    Option(loader match {
      case SystemV => "2 3 4 5"
      case Upstart => "[2345]"
      case Systemd => null
    })
  }

  private[this] def defaultStopRunlevels(loader: ServerLoader): Option[String] = {
    Option(loader match {
      case SystemV => "0 1 6"
      case Upstart => "[016]"
      case Systemd => null
    })
  }

  private[this] def getStartScriptLocation(loader: ServerLoader): String = {
    loader match {
      case Upstart => "/etc/init/"
      case SystemV => "/etc/init.d/"
      case Systemd => "/usr/lib/systemd/system/"
    }
  }

  /* Find the template source for the given Server loading scheme, with cascading fallback
   * If the serverLoader scheme is SystemD, then searches for files in this order:
   *
   * (assuming sourceDirectory is `src`)
   *
   * - src/templates/etc-default-systemd
   * - src/templates/etc-default
   * - Provided template
   */

  private[this] def getEtcTemplateSource(sourceDirectory: File, loader: ServerLoader): java.net.URL = {
    val (suffix, default) = loader match {
      case Upstart =>
        ("-upstart", getClass.getResource(ETC_DEFAULT + "-template"))
      case SystemV =>
        ("-systemv", getClass.getResource(ETC_DEFAULT + "-template"))
      case Systemd =>
        ("-systemd", getClass.getResource(ETC_DEFAULT + "-systemd-template"))
    }

    val overrides = List[File](
      sourceDirectory / "templates" / (ETC_DEFAULT + suffix),
      sourceDirectory / "templates" / ETC_DEFAULT)
    overrides.
      find(_.exists).
      map(_.toURI.toURL).
      getOrElse(default)
  }

  // Used to tell our packager to install our /etc/default/{{appName}} config file.
  protected def etcDefaultMapping(conf: Option[File], envLocation: Option[String]): Seq[LinuxPackageMapping] = {
    val mapping = for (
      path <- envLocation;
      c <- conf
    ) yield LinuxPackageMapping(Seq(c -> path), LinuxFileMetaData(Users.Root, Users.Root, "644")).withConfig()

    mapping.toSeq
  }

  protected def startScriptMapping(name: String, script: Option[File], loader: ServerLoader, scriptDir: String, scriptName: Option[String]): Seq[LinuxPackageMapping] = {
    val (path, permissions, isConf) = loader match {
      case Upstart => ("/etc/init/" + scriptName.getOrElse(name + ".conf"), "0644", "true")
      case SystemV => ("/etc/init.d/" + scriptName.getOrElse(name), "0755", "false")
      case Systemd => ("/usr/lib/systemd/system/" + scriptName.getOrElse(name + ".service"), "0644", "true")
    }
    for {
      s <- script.toSeq
    } yield LinuxPackageMapping(Seq(s -> path), LinuxFileMetaData(Users.Root, Users.Root, permissions, isConf))
  }

  protected def makeStartScript(template: URL, replacements: Seq[(String, String)], tmpDir: File, loader: ServerLoader): Option[File] = {
    val scriptBits = TemplateWriter generateScript (template, replacements)
    val script = tmpDir / "tmp" / "bin" / s"$loader-init"
    IO.write(script, scriptBits)
    Some(script)
  }

  /**
   *
   * @param config for which plugin (Debian, Rpm)
   * @param replacements for the placeholders
   * @param scriptName that should be loaded
   * @return script lines
   */
  private[this] def getScriptContent(config: Configuration, replacements: Seq[(String, String)])(scriptName: String): String = {
    JavaServerBashScript(scriptName, ARCHETYPE, config, replacements) getOrElse {
      sys.error(s"Couldn't load [$scriptName] for config [${config.name}] in archetype [$ARCHETYPE]")
    }
  }

  /**
   * Creates the etc-default file, which will contain the basic configuration
   * for an app.
   *
   * @param name of the etc-default config file
   * @param tmpDir to store the resulting file in (e.g. target in Universal)
   * @param source of etc-default script
   * @param replacements for placeholders in etc-default script
   *
   * @return Some(file: File)
   */
  protected def makeEtcDefaultScript(
    name: String, tmpDir: File, source: java.net.URL, replacements: Seq[(String, String)]): Option[File] = {
    val scriptBits = TemplateWriter.generateScript(source, replacements)
    val script = tmpDir / "tmp" / "etc" / "default" / name
    IO.write(script, scriptBits)
    Some(script)
  }

  /**
   *
   *
   * @param scriptDirectory
   * @param scripts
   * @param replacements
   */
  protected def rpmScriptletContents(scriptDirectory: File, scripts: Map[String, Seq[String]], replacements: Seq[(String, String)]): Map[String, Seq[String]] = {
    import RpmConstants._
    val predefined = List(Pre, Post, Preun, Postun)
    val predefinedScripts = predefined.foldLeft(scripts) {
      case (scripts, script) =>
        val userDefined = Option(scriptDirectory / script) collect {
          case file if file.exists && file.isFile => file.toURI.toURL
        }
        // generate content
        val content = JavaServerBashScript(script, ARCHETYPE, Rpm, replacements, userDefined).map {
          script => TemplateWriter generateScriptFromString (script, replacements)
        }.toSeq
        // add new content
        val newContent = scripts.getOrElse(script, Nil) ++ content.toSeq
        scripts + (script -> newContent)
    }

    // used to override template
    val rpmScripts = Option(scriptDirectory.listFiles) getOrElse Array.empty

    // remove all non files and already processed templates
    rpmScripts.filter(s => s.isFile && !predefined.contains(s.getName)).foldLeft(predefinedScripts) {
      case (scripts, scriptlet) =>
        val script = scriptlet.getName
        val existingContent = scripts.getOrElse(script, Nil)

        val loadedContent = JavaServerBashScript(script, ARCHETYPE, Rpm, replacements, Some(scriptlet.toURI.toURL)).map {
          script => TemplateWriter generateScriptFromString (script, replacements)
        }.toSeq
        // add the existing and loaded content
        scripts + (script -> (existingContent ++ loadedContent))
    }
  }
}
