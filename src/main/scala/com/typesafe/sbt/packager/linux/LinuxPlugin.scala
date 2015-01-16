package com.typesafe.sbt
package packager
package linux

import sbt._
import sbt.Keys.{ name, normalizedName, mappings, sourceDirectory }
import linux.LinuxPlugin.Users
import packager.Keys._
import packager.archetypes.{ ServerLoader, TemplateWriter }
import SbtNativePackager.Universal

/**
 * Plugin containing all the generic values used for
 * packaging linux software.
 *
 * @example Enable the plugin in the `build.sbt`
 * {{{
 *    enablePlugins(LinuxPlugin)
 * }}}
 */
object LinuxPlugin extends AutoPlugin {

  override def requires = universal.UniversalPlugin
  override lazy val projectSettings = linuxSettings ++ mapGenericFilesToLinux

  object autoImport extends LinuxKeys with LinuxMappingDSL {
    val Linux = config("linux")
  }

  import autoImport._

  /** default users available for */
  object Users {
    val Root = "root"
  }
  /** key for replacement in linuxScriptReplacements */
  val CONTROL_FUNCTIONS = "control-functions"

  def controlFunctions(): URL = getClass getResource CONTROL_FUNCTIONS

  /**
   * default linux settings
   */
  def linuxSettings: Seq[Setting[_]] = Seq(
    linuxPackageMappings := Seq.empty,
    linuxPackageSymlinks := Seq.empty,
    sourceDirectory in Linux <<= sourceDirectory apply (_ / "linux"),
    generateManPages <<= (sourceDirectory in Linux, sbt.Keys.streams) map { (dir, s) =>
      for (file <- (dir / "usr/share/man/man1" ** "*.1").get) {
        val man = makeMan(file)
        s.log.info("Generated man page for[" + file + "] =")
        s.log.info(man)
      }
    },
    packageSummary in Linux <<= packageSummary,
    packageDescription in Linux <<= packageDescription,
    name in Linux <<= name,
    packageName in Linux <<= packageName,
    executableScriptName in Linux <<= executableScriptName,
    daemonUser in Linux <<= packageName in Linux,
    daemonUserUid in Linux := None,
    daemonGroup in Linux <<= daemonUser in Linux,
    daemonGroupGid in Linux := None,
    daemonShell in Linux := "/bin/false",
    defaultLinuxInstallLocation := "/usr/share",
    defaultLinuxLogsLocation := "/var/log",
    defaultLinuxConfigLocation := "/etc",

    // Default settings for service configurations
    startRunlevels := None,
    stopRunlevels := None,
    requiredStartFacilities := None,
    requiredStopFacilities := None,
    termTimeout := 60,
    killTimeout := 30,

    // Default linux bashscript replacements
    linuxScriptReplacements := makeReplacements(
      author = (maintainer in Linux).value,
      description = (packageSummary in Linux).value,
      execScript = (executableScriptName in Linux).value,
      chdir = s"${defaultLinuxInstallLocation.value}/${(packageName in Linux).value}",
      appName = (packageName in Linux).value,
      version = sbt.Keys.version.value,
      daemonUser = (daemonUser in Linux).value,
      daemonUserUid = (daemonUserUid in Linux).value,
      daemonGroup = (daemonGroup in Linux).value,
      daemonGroupGid = (daemonGroupGid in Linux).value,
      daemonShell = (daemonShell in Linux).value,
      termTimeout = (termTimeout in Linux).value,
      killTimeout = (killTimeout in Linux).value
    ),
    linuxScriptReplacements += controlScriptFunctionsReplacement( /* Add key for control-functions */ )

  )

  /**
   * maps the `mappings` content into `linuxPackageMappings` and
   * `linuxPackageSymlinks`.
   */
  def mapGenericFilesToLinux: Seq[Setting[_]] = Seq(

    // First we look at the src/linux files
    linuxPackageMappings <++= (sourceDirectory in Linux) map { dir =>
      mapGenericMappingsToLinux(MappingsHelper contentOf dir, Users.Root, Users.Root)(identity)
    },
    // Now we look at the src/universal files.
    linuxPackageMappings <++= (packageName in Linux, mappings in Universal, defaultLinuxInstallLocation) map {
      (pkg, mappings, installLocation) =>
        // TODO - More windows filters...
        def isWindowsFile(f: (File, String)): Boolean =
          f._2 endsWith ".bat"

        mapGenericMappingsToLinux(mappings filterNot isWindowsFile, Users.Root, Users.Root) { name =>
          installLocation + "/" + pkg + "/" + name
        }
    },
    // Now we generate symlinks.
    linuxPackageSymlinks <++= (packageName in Linux, mappings in Universal, defaultLinuxInstallLocation) map { (pkg, mappings, installLocation) =>
      for {
        (file, name) <- mappings
        if !file.isDirectory
        if name startsWith "bin/"
        if !(name endsWith ".bat") // IGNORE windows-y things.
      } yield LinuxSymlink("/usr/" + name, installLocation + "/" + pkg + "/" + name)
    },
    // Map configuration files
    linuxPackageSymlinks <++= (packageName in Linux, mappings in Universal, defaultLinuxInstallLocation, defaultLinuxConfigLocation)
      map { (pkg, mappings, installLocation, configLocation) =>
        val needsConfLink =
          mappings exists {
            case (file, name) =>
              (name startsWith "conf/") && !file.isDirectory
          }
        if (needsConfLink) Seq(LinuxSymlink(
          link = configLocation + "/" + pkg,
          destination = installLocation + "/" + pkg + "/conf"
        ))
        else Seq.empty
      }
  )

  /**
   *
   * @param author -
   * @param description - short description
   * @param execScript - name of the script in /usr/bin
   * @param chdir - execution path of the script
   * @param retries - on fail, how often should a restart be tried
   * @param retryTimeout - pause between retries
   * @return Seq of placeholder>replacement pairs
   */
  def makeReplacements(
    author: String,
    description: String,
    execScript: String,
    chdir: String,
    appName: String,
    version: String,
    daemonUser: String,
    daemonUserUid: Option[String],
    daemonGroup: String,
    daemonGroupGid: Option[String],
    daemonShell: String,
    retries: Int = 0,
    retryTimeout: Int = 60,
    termTimeout: Int = 60,
    killTimeout: Int = 30
  ): Seq[(String, String)] =
    Seq(
      "author" -> author,
      "descr" -> description,
      "exec" -> execScript,
      "chdir" -> chdir,
      "retries" -> retries.toString,
      "retryTimeout" -> retryTimeout.toString,
      "app_name" -> appName,
      "version" -> version,
      "daemon_user" -> daemonUser,
      "daemon_user_uid" -> daemonUserUid.getOrElse(""),
      "daemon_group" -> daemonGroup,
      "daemon_group_gid" -> daemonGroupGid.getOrElse(""),
      "daemon_shell" -> daemonShell,
      "term_timeout" -> termTimeout.toString,
      "kill_timeout" -> killTimeout.toString
    )

  /**
   * Load the default controlscript functions which contain
   * addUser/removeUser/addGroup/removeGroup
   *
   * @return placeholder->content
   */
  def controlScriptFunctionsReplacement(template: Option[URL] = None): (String, String) = {
    val url = template getOrElse LinuxPlugin.controlFunctions
    LinuxPlugin.CONTROL_FUNCTIONS -> TemplateWriter.generateScript(source = url, replacements = Nil)
  }

  // TODO - we'd like a set of conventions to take universal mappings and create linux package mappings.

  /** Create a ascii friendly string for a man page. */
  final def makeMan(file: File): String =
    Process("groff -man -Tascii " + file.getAbsolutePath).!!

  // This method wires a lot of hand-coded generalities about how to map directories
  // into linux, and the conventions we expect.
  // It is by no means 100% accurate, but should be ok for the simplest cases.
  // For advanced users, use the underlying APIs.
  // Right now, it's also pretty focused on command line scripts packages.

  /**
   * Maps linux file format from the universal from the conventions:
   *
   * `<project>/src/linux` files are mapped directly into linux packages.
   * `<universal>` files are placed under `/usr/share/<package-name>`
   * `<universal>/bin` files are given symlinks in `/usr/bin`
   * `<universal>/conf` directory is given a symlink to `/etc/<package-name>`
   * Files in `conf/` or `etc/` directories are automatically marked as configuration.
   * `../man/...1` files are automatically compressed into .gz files.
   *
   */
  def mapGenericMappingsToLinux(mappings: Seq[(File, String)], user: String, group: String)(rename: String => String): Seq[LinuxPackageMapping] = {
    val (directories, nondirectories) = mappings.partition(_._1.isDirectory)
    val (binaries, nonbinaries) = nondirectories.partition(_._1.canExecute)
    val (manPages, nonManPages) = nonbinaries partition {
      case (file, name) => (name contains "man/") && (name endsWith ".1")
    }
    val compressedManPages =
      for ((file, name) <- manPages)
        yield file -> (name + ".gz")
    val (configFiles, remaining) = nonManPages partition {
      case (file, name) => (name contains "etc/") || (name contains "conf/")
    }
    def packageMappingWithRename(mappings: (File, String)*): LinuxPackageMapping = {
      val renamed =
        for ((file, name) <- mappings)
          yield file -> rename(name)
      packageMapping(renamed: _*)
    }

    Seq(
      packageMappingWithRename((binaries ++ directories): _*) withUser user withGroup group withPerms "0755",
      packageMappingWithRename(compressedManPages: _*).gzipped withUser user withGroup group withPerms "0644",
      packageMappingWithRename(configFiles: _*) withConfig () withUser user withGroup group withPerms "0644",
      packageMappingWithRename(remaining: _*) withUser user withGroup group withPerms "0644"
    )
  }

}
