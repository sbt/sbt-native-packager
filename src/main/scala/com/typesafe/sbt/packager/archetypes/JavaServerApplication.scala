package com.typesafe.sbt.packager.archetypes

import sbt._
import sbt.Keys.{ target, mainClass, sourceDirectory, streams, javaOptions, run }
import com.typesafe.sbt.SbtNativePackager.{ Debian, Rpm, Universal, Linux }
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.linux.{ LinuxFileMetaData, LinuxPackageMapping, LinuxSymlink, LinuxPlugin }
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.packageTemplateMapping
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin.autoImport.RpmConstants
import com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader

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
      // === Extra replacements ===
      linuxScriptReplacements ++= bashScriptEnvConfigLocation.value.map(ENV_CONFIG_REPLACEMENT -> _).toSeq,
      linuxScriptReplacements += Names.DaemonStdoutLogFileReplacement -> daemonStdoutLogFile.value.getOrElse(""),

      // === Maintainer scripts ===
      maintainerScripts := {
        val scripts = (maintainerScripts in Debian).value
        val replacements = (linuxScriptReplacements in Debian).value
        val contentOf = getScriptContent(Debian, replacements) _

        scripts ++ Map(
          Preinst -> (scripts.getOrElse(Preinst, Nil) ++ contentOf(Preinst)),
          Postinst -> (scripts.getOrElse(Postinst, Nil) ++ contentOf(Postinst)),
          Prerm -> (scripts.getOrElse(Prerm, Nil) ++ contentOf(Prerm)),
          Postrm -> (scripts.getOrElse(Postrm, Nil) ++ contentOf(Postrm))
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
      // === Extra replacements ===
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
      // == Maintainer scripts ===
      maintainerScripts in Rpm := rpmScriptletContents(rpmScriptsDirectory.value, (maintainerScripts in Rpm).value, (linuxScriptReplacements in Rpm).value)
    )
  }

  /* ==========================================  */
  /* ============ Helper Methods ==============  */
  /* ==========================================  */


  /* Find the template source for the given Server loading scheme, with cascading fallback
   * If the serverLoader scheme is SystemD, then searches for files in this order:
   *
   * (assuming sourceDirectory is `src`)
   *
   * - src/templates/etc-default-systemd
   * - src/templates/etc-default
   * - Provided template
   */
  private[this] def getEtcTemplateSource(sourceDirectory: File, loader: Option[ServerLoader]): java.net.URL = {
    val defaultTemplate = getClass.getResource(ETC_DEFAULT + "-template")
    val (suffix, default) = loader.map {
      case Upstart => ("-upstart", defaultTemplate)
      case SystemV => ("-systemv", defaultTemplate)
      case Systemd => ("-systemd", getClass.getResource(ETC_DEFAULT + "-systemd-template"))
    }.getOrElse(("", defaultTemplate))

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

  /**
   * Loads an available script from the native-packager source if available.
   * 
   * @param config for which plugin (Debian, Rpm)
   * @param replacements for the placeholders
   * @param scriptName that should be loaded
   * @return script lines
   */
  private[this] def getScriptContent(config: Configuration, replacements: Seq[(String, String)])(scriptName: String): Seq[String] = {
    JavaServerBashScript(scriptName, ARCHETYPE, config, replacements).toSeq
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
    rpmScripts.diff(predefined).filter(_.isFile).foldLeft(predefinedScripts) {
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
