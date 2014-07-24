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

    linuxJavaAppStartScriptBuilder := JavaAppStartScript.Debian,
    // This one is begging for sbt 0.13 syntax...
    linuxScriptReplacements <<= (
      maintainer in Linux, packageSummary in Linux, daemonUser in Linux, daemonGroup in Linux, daemonShell in Linux,
      packageName in Linux, executableScriptName in Linux,
      sbt.Keys.version, defaultLinuxInstallLocation, linuxJavaAppStartScriptBuilder)
      apply { (author, descr, daemonUser, daemonGroup, daemonShell, name, execScript, version, installLocation, builder) =>
        val appDir = installLocation + "/" + name

        // TODO Making replacements should be done somewhere else. Maybe TemplateWriter
        builder.makeReplacements(
          author = author,
          description = descr,
          execScript = execScript,
          chdir = appDir,
          appName = name,
          daemonUser = daemonUser,
          daemonGroup = daemonGroup,
          daemonShell = daemonShell)
      }

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