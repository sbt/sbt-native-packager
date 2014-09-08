package com.typesafe.sbt
package packager
package linux

import Keys._
import sbt._
import sbt.Keys.{ normalizedName }
import packager.Keys.{
  defaultLinuxInstallLocation,
  defaultLinuxConfigLocation,
  defaultLinuxLogsLocation
}
import com.typesafe.sbt.packager.linux.LinuxPlugin.Users
import com.typesafe.sbt.packager.archetypes.{ ServerLoader, JavaAppStartScript }

/**
 * Plugin trait containing all the generic values used for
 * packaging linux software.
 */
trait LinuxPlugin extends Plugin {
  // TODO - is this needed
  val Linux = config("linux")

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
    daemonGroup in Linux <<= daemonUser in Linux,
    daemonShell in Linux := "/bin/false",
    defaultLinuxInstallLocation := "/usr/share",
    defaultLinuxLogsLocation := "/var/log",
    defaultLinuxConfigLocation := "/etc",

    // Default linux bashscript replacements
    linuxScriptReplacements := makeReplacements(
      author = (maintainer in Linux).value,
      description = (packageSummary in Linux).value,
      execScript = (executableScriptName in Linux).value,
      chdir = s"${defaultLinuxInstallLocation.value}/${(packageName in Linux).value}",
      appName = (packageName in Linux).value,
      version = sbt.Keys.version.value,
      daemonUser = (daemonUser in Linux).value,
      daemonGroup = (daemonGroup in Linux).value,
      daemonShell = (daemonShell in Linux).value
    )

  )

  /** DSL for packaging files into .deb */
  def packageMapping(files: (File, String)*) = LinuxPackageMapping(files)

  /**
   * @param dir - use some directory, e.g. target.value
   * @param files
   */
  def packageTemplateMapping(files: String*)(dir: File = new File(sys.props("java.io.tmpdir"))) = LinuxPackageMapping(files map ((dir, _)))

  // TODO can the packager.MappingsHelper be used here?
  /**
   * @see #mapDirectoryAndContents
   * @param dirs - directories to map
   */
  def packageDirectoryAndContentsMapping(dirs: (File, String)*) = LinuxPackageMapping(mapDirectoryAndContents(dirs: _*))

  /**
   * This method includes files and directories.
   *
   * @param dirs - directories to map
   */
  def mapDirectoryAndContents(dirs: (File, String)*): Seq[(File, String)] = for {
    (src, dest) <- dirs
    path <- (src ***).get
  } yield path -> path.toString.replaceFirst(src.toString, dest)

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
    daemonGroup: String,
    daemonShell: String,
    retries: Int = 0,
    retryTimeout: Int = 60): Seq[(String, String)] =
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
      "daemon_group" -> daemonGroup,
      "daemon_shell" -> daemonShell)

  // TODO - we'd like a set of conventions to take universal mappings and create linux package mappings.

  /** Create a ascii friendly string for a man page. */
  final def makeMan(file: File): String =
    Process("groff -man -Tascii " + file.getAbsolutePath).!!
}

object LinuxPlugin {
  object Users {
    val Root = "root"
  }
}