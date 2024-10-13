package com.typesafe.sbt.packager.linux

import sbt._
import sbt.Keys.{mappings, name, sourceDirectory, streams}
import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.MappingsHelper
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.archetypes.TemplateWriter

/**
  * Plugin containing all the generic values used for packaging linux software.
  *
  * @example
  *   Enable the plugin in the `build.sbt`
  *   {{{
  *    enablePlugins(LinuxPlugin)
  *   }}}
  */
object LinuxPlugin extends AutoPlugin {

  override def requires = UniversalPlugin
  override lazy val projectSettings = linuxSettings ++ mapGenericFilesToLinux

  object autoImport extends LinuxKeys with LinuxMappingDSL {
    val Linux = config("linux")
  }

  import autoImport._

  override def projectConfigurations: Seq[Configuration] = Seq(Linux)

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
  def linuxSettings: Seq[Setting[_]] =
    Seq(
      linuxPackageMappings := Seq.empty,
      linuxPackageSymlinks := Seq.empty,
      Linux / sourceDirectory := sourceDirectory.value / "linux",
      generateManPages := {
        val log = streams.value.log
        for (file <- ((Linux / sourceDirectory).value / "usr/share/man/man1" ** "*.1").get) {
          val man = makeMan(file)
          log.info("Generated man page for[" + file + "] =")
          log.info(man)
        }
      },
      Linux / packageSummary := packageSummary.value,
      Linux / packageDescription := packageDescription.value,
      Linux / name := name.value,
      Linux / packageName := packageName.value,
      Linux / executableScriptName := executableScriptName.value,
      daemonUser := (Linux / packageName).value,
      Linux / daemonUser := daemonUser.value,
      Linux / daemonUserUid := None,
      daemonGroup := (Linux / daemonUser).value,
      Linux / daemonGroup := daemonGroup.value,
      Linux / daemonGroupGid := None,
      Linux / daemonShell := "/bin/false",
      Linux / daemonHome := s"/var/lib/${(Linux / daemonUser).value}",
      defaultLinuxInstallLocation := "/usr/share",
      defaultLinuxLogsLocation := "/var/log",
      defaultLinuxConfigLocation := "/etc",
      // Default settings for service configurations
      startRunlevels := None,
      stopRunlevels := None,
      requiredStartFacilities := None,
      requiredStopFacilities := None,
      fileDescriptorLimit := Some("1024"),
      termTimeout := 10,
      killTimeout := 10,
      // Default linux bashscript replacements
      linuxScriptReplacements := makeReplacements(
        author = (Linux / maintainer).value,
        description = (Linux / packageSummary).value,
        execScript = (Linux / executableScriptName).value,
        chdir = chdir(defaultLinuxInstallLocation.value, (Linux / packageName).value),
        logdir = defaultLinuxLogsLocation.value,
        appName = (Linux / packageName).value,
        version = sbt.Keys.version.value,
        daemonUser = (Linux / daemonUser).value,
        daemonUserUid = (Linux / daemonUserUid).value,
        daemonGroup = (Linux / daemonGroup).value,
        daemonGroupGid = (Linux / daemonGroupGid).value,
        daemonShell = (Linux / daemonShell).value,
        daemonHome = (Linux / daemonHome).value,
        fileDescriptorLimit = (Linux / fileDescriptorLimit).value
      ),
      linuxScriptReplacements += controlScriptFunctionsReplacement( /* Add key for control-functions */ ),
      Linux / maintainerScripts := Map.empty
    )

  /**
    * maps the `mappings` content into `linuxPackageMappings` and `linuxPackageSymlinks`.
    */
  def mapGenericFilesToLinux: Seq[Setting[_]] =
    Seq(
      // First we look at the src/linux files
      linuxPackageMappings ++= {
        val linuxContent = MappingsHelper.contentOf((Linux / sourceDirectory).value)
        if (linuxContent.isEmpty) Seq.empty
        else mapGenericMappingsToLinux(linuxContent, Users.Root, Users.Root)(identity)
      },
      // Now we look at the src/universal files.
      linuxPackageMappings ++= getUniversalFolderMappings(
        (Linux / packageName).value,
        defaultLinuxInstallLocation.value,
        (Universal / mappings).value
      ),
      // Now we generate symlinks.
      linuxPackageSymlinks ++= {
        val installLocation = defaultLinuxInstallLocation.value
        val linuxPackageName = (Linux / packageName).value
        for {
          (file, name) <- (Universal / mappings).value
          if !file.isDirectory
          if name startsWith "bin/"
          if !(name endsWith ".bat") // IGNORE windows-y things.
        } yield LinuxSymlink("/usr/" + name, installLocation + "/" + linuxPackageName + "/" + name)
      },
      // Map configuration files
      linuxPackageSymlinks ++= {
        val linuxPackageName = (Linux / packageName).value
        val installLocation = defaultLinuxInstallLocation.value
        val configLocation = defaultLinuxConfigLocation.value
        val needsConfLink =
          (Universal / mappings).value exists { case (file, destination) =>
            (destination startsWith "conf/") && !file.isDirectory
          }
        if (needsConfLink)
          Seq(
            LinuxSymlink(
              link = configLocation + "/" + linuxPackageName,
              destination = installLocation + "/" + linuxPackageName + "/conf"
            )
          )
        else Seq.empty
      }
    )

  def makeReplacements(
    author: String,
    description: String,
    execScript: String,
    chdir: String,
    logdir: String,
    appName: String,
    version: String,
    daemonUser: String,
    daemonUserUid: Option[String],
    daemonGroup: String,
    daemonGroupGid: Option[String],
    daemonShell: String,
    daemonHome: String,
    fileDescriptorLimit: Option[String]
  ): Seq[(String, String)] =
    Seq(
      "author" -> author,
      "descr" -> description,
      "exec" -> execScript,
      "chdir" -> chdir,
      "logdir" -> logdir,
      "app_name" -> appName,
      "version" -> version,
      "daemon_user" -> daemonUser,
      "daemon_user_uid" -> daemonUserUid.getOrElse(""),
      "daemon_group" -> daemonGroup,
      "daemon_group_gid" -> daemonGroupGid.getOrElse(""),
      "daemon_shell" -> daemonShell,
      "daemon_home" -> daemonHome,
      "file_descriptor_limit" -> fileDescriptorLimit.getOrElse("")
    )

  /**
    * Load the default controlscript functions which contain addUser/removeUser/addGroup/removeGroup
    *
    * @return
    *   placeholder->content
    */
  def controlScriptFunctionsReplacement(template: Option[URL] = None): (String, String) = {
    val url = template getOrElse LinuxPlugin.controlFunctions
    LinuxPlugin.CONTROL_FUNCTIONS -> TemplateWriter.generateScript(source = url, replacements = Nil)
  }

  // TODO - we'd like a set of conventions to take universal mappings and create linux package mappings.

  /** Create a ascii friendly string for a man page. */
  final def makeMan(file: File): String =
    sys.process.Process("groff -man -Tascii " + file.getAbsolutePath).!!

  // This method wires a lot of hand-coded generalities about how to map directories
  // into linux, and the conventions we expect.
  // It is by no means 100% accurate, but should be ok for the simplest cases.
  // For advanced users, use the underlying APIs.
  // Right now, it's also pretty focused on command line scripts packages.

  /**
    * Maps linux file format from the universal from the conventions:
    *
    *   - `<project>/src/linux` files are mapped directly into linux packages.
    *   - `<universal>` files are placed under `/usr/share/<package-name>`
    *   - `<universal>/bin` files are given symlinks in `/usr/bin`
    *   - `<universal>/conf` directory is given a symlink to `/etc/<package-name>`
    *   - Files in `conf/` or `etc/` directories are automatically marked as configuration.
    *   - `../man/...1` files are automatically compressed into .gz files.
    */
  def mapGenericMappingsToLinux(mappings: Seq[(File, String)], user: String, group: String)(
    rename: String => String
  ): Seq[LinuxPackageMapping] = {
    val (directories, nondirectories) = mappings.partition(_._1.isDirectory)
    val (configFiles, nonConfigFiles) = nondirectories partition { case (_, destination) =>
      (destination contains "etc/") || (destination contains "conf/")
    }
    val (binaries, nonbinaries) = nonConfigFiles.partition(_._1.canExecute)
    val (manPages, remaining) = nonbinaries partition { case (_, destination) =>
      (destination contains "man/") && (destination endsWith ".1")
    }
    val compressedManPages =
      for ((file, name) <- manPages)
        yield file -> (name + ".gz")

    def packageMappingWithRename(mappings: (File, String)*): LinuxPackageMapping = {
      val renamed =
        for ((file, name) <- mappings)
          yield file -> rename(name)
      packageMapping(renamed: _*)
    }

    Seq(
      packageMappingWithRename(binaries ++ directories: _*) withUser user withGroup group withPerms "0755",
      packageMappingWithRename(compressedManPages: _*).gzipped withUser user withGroup group withPerms "0644",
      packageMappingWithRename(configFiles: _*) withConfig () withUser user withGroup group withPerms "0644",
      packageMappingWithRename(remaining: _*) withUser user withGroup group withPerms "0644"
    )
  }

  final def chdir(installLocation: String, packageName: String): String =
    s"$installLocation/$packageName"

  private[this] def getUniversalFolderMappings(
    pkg: String,
    installLocation: String,
    mappings: Seq[(File, String)]
  ): Seq[LinuxPackageMapping] = {
    // TODO - More windows filters...
    def isWindowsFile(f: (File, String)): Boolean =
      f._2 endsWith ".bat"

    val filtered = mappings.filterNot(isWindowsFile)

    if (filtered.isEmpty) Seq.empty
    else
      mapGenericMappingsToLinux(filtered, Users.Root, Users.Root) { name =>
        installLocation + "/" + pkg + "/" + name
      }
  }
}
