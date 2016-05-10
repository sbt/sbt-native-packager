package com.typesafe.sbt
package packager
package debian

import sbt._
import sbt.Keys.{ streams, name, version, sourceDirectory, target, packageBin, TaskStreams }
import packager.Keys._
import packager.Hashing
import linux.LinuxPlugin.autoImport.{
  packageArchitecture,
  linuxScriptReplacements,
  linuxPackageMappings,
  linuxPackageSymlinks,
  serverLoading,
  daemonShell
}
import linux.{ LinuxFileMetaData, LinuxPackageMapping, LinuxSymlink }
import linux.LinuxPlugin.Users
import universal.Archives
import archetypes.TemplateWriter
import SbtNativePackager.{ Universal, Linux }

/**
  * == Debian Plugin ==
  *
  * This plugin provides the ability to build ''.deb'' packages.
  *
  * == Configuration ==
  *
  * In order to configure this plugin take a look at the available [[com.typesafe.sbt.packager.debian.DebianKeys]]
  *
  * @example Enable the plugin in the `build.sbt`. By default this will use
  * the native debian packaging implementation [[com.typesafe.sbt.packager.debian.DebianNativePackaging]].
  * {{{
  *    enablePlugins(DebianPlugin)
  * }}}
  *
  */
object DebianPlugin extends AutoPlugin with DebianNativePackaging {

  override def requires = linux.LinuxPlugin

  object autoImport extends DebianKeys {
    val Debian = config("debian") extend Linux
    val DebianConstants = Names
  }

  import autoImport._

  /** Debian constants */
  object Names {
    val DebianSource = "debian"
    val DebianMaintainerScripts = "DEBIAN"

    //maintainer script names
    val Postinst = "postinst"
    val Postrm = "postrm"
    val Prerm = "prerm"
    val Preinst = "preinst"

    val Control = "control"
    val Conffiles = "conffiles"

    val Changelog = "changelog"
    val Files = "files"
  }

  val CHOWN_REPLACEMENT = "chown-paths"

  override def projectConfigurations: Seq[Configuration] =  Seq(Debian)

  // TODO maybe we can put settings/debiansettings together
  /**
    * Enables native packaging by default
    */
  override lazy val projectSettings = settings ++ debianSettings ++ debianNativeSettings

  /**
    * the default debian settings for the debian namespaced settings
    */
  private def settings = Seq(
    /* ==== Debian default settings ==== */
    debianPriority := "optional",
    debianSection := "java",
    debianPackageDependencies := Seq.empty,
    debianPackageRecommends := Seq.empty,
    debianSignRole := "builder",
    target in Debian <<= (target, name in Debian, version in Debian) apply ((t, n, v) => t / (n + "-" + v)),
    name in Debian <<= (name in Linux),
    // TODO maybe we can remove this, with the projectConfigurations
    maintainerScripts in Debian <<= (maintainerScripts in Linux),
    packageName in Debian <<= (packageName in Linux),
    executableScriptName in Debian <<= (executableScriptName in Linux),
    version in Debian <<= (version in Linux),
    linuxPackageMappings in Debian <<= linuxPackageMappings,
    packageDescription in Debian <<= packageDescription in Linux,
    packageSummary in Debian <<= packageSummary in Linux,
    maintainer in Debian <<= maintainer in Linux,

    // override the linux sourceDirectory setting
    sourceDirectory in Debian <<= sourceDirectory,

    /* ==== Debian configuration settings ==== */
    debianControlScriptsDirectory <<= (sourceDirectory) apply (_ / "debian" / Names.DebianMaintainerScripts),
    debianMaintainerScripts := Seq.empty,
    debianMakePreinstScript := None,
    debianMakePrermScript := None,
    debianMakePostinstScript := None,
    debianMakePostrmScript := None,
    debianChangelog := None,

    /* === new debian scripts implementation */
    maintainerScripts in Debian := {
      val replacements = (linuxScriptReplacements in Debian).value
      val scripts = Map(
        Names.Prerm -> defaultMaintainerScript(Names.Prerm).toSeq.flatten,
        Names.Preinst -> defaultMaintainerScript(Names.Preinst).toSeq.flatten,
        Names.Postinst -> defaultMaintainerScript(Names.Postinst).toSeq.flatten,
        Names.Postrm -> defaultMaintainerScript(Names.Postrm).toSeq.flatten
      )

      // this is for legacy purposes to keep old behaviour
      // --- legacy starts
      def readContent(scriptFiles: Seq[(File, String)]): Map[String, Seq[String]] = scriptFiles.map {
        case (scriptFile, scriptName) => scriptName -> IO.readLines(scriptFile)
      }.toMap

      val userProvided = readContent(Seq(
        debianMakePreinstScript.value.map(script => script -> Names.Preinst),
        debianMakePostinstScript.value.map(script => script -> Names.Postinst),
        debianMakePrermScript.value.map(script => script -> Names.Prerm),
        debianMakePostrmScript.value.map(script => script -> Names.Postrm)
      ).flatten)

      // these things get appended. Don't check for nonexisting keys as they are already in the default scripts map
      val appendedScripts = scripts.map {
        case (scriptName, content) => scriptName -> (content ++ userProvided.getOrElse(scriptName, Nil))
      }
      // override and merge with the user defined scripts. Will change in the future
      val controlScriptsDir = debianControlScriptsDirectory.value
      val overridenScripts = scripts ++ readContent(Seq(
        scriptMapping(Names.Prerm, debianMakePrermScript.value, controlScriptsDir),
        scriptMapping(Names.Preinst, debianMakePreinstScript.value, controlScriptsDir),
        scriptMapping(Names.Postinst, debianMakePostinstScript.value, controlScriptsDir),
        scriptMapping(Names.Postrm, debianMakePostrmScript.value, controlScriptsDir)
      ).flatten)
      // --- legacy ends

      // TODO remove the overridenScripts
      val content = appendedScripts ++ overridenScripts

      // apply all replacements
      content.mapValues { lines =>
        TemplateWriter.generateScriptFromLines(lines, replacements)
      }
    },
    debianMaintainerScripts := generateDebianMaintainerScripts(
      (maintainerScripts in Debian).value,
      (linuxScriptReplacements in Debian).value,
      (target in Universal).value
    ),
    debianNativeBuildOptions := Nil
  )

  /**
    * == Debian scoped settings ==
    * Everything used inside the debian scope
    *
    */
  private def debianSettings: Seq[Setting[_]] = inConfig(Debian)(
    Seq(
      packageArchitecture := "all",
      debianPackageInfo <<= (packageName, version, maintainer, packageSummary, packageDescription) apply PackageInfo,
      debianPackageMetadata <<= (debianPackageInfo, debianPriority, packageArchitecture, debianSection,
        debianPackageDependencies, debianPackageRecommends) apply PackageMetaData,
      debianPackageInstallSize <<= linuxPackageMappings map { mappings =>
        (for {
          LinuxPackageMapping(files, _, zipped) <- mappings
          (file, _) <- files
          if !file.isDirectory && file.exists
          // TODO - If zipped, heuristically figure out a reduction factor.
        } yield file.length).sum / 1024
      },
      debianControlFile <<= (debianPackageMetadata, debianPackageInstallSize, target) map {
        (data, size, dir) =>
          if (data.info.description == null || data.info.description.isEmpty) {
            sys.error(
              """packageDescription in Debian cannot be empty. Use
                 packageDescription in Debian := "My package Description""""
            )
          }
    val cfile = dir / Names.DebianMaintainerScripts / Names.Control
          IO.write(cfile, data.makeContent(size), java.nio.charset.Charset.defaultCharset)
          chmod(cfile, "0644")
          cfile
      },
      debianConffilesFile <<= (linuxPackageMappings, target) map {
        (mappings, dir) =>
    val cfile = dir / Names.DebianMaintainerScripts / Names.Conffiles
          val conffiles = for {
            LinuxPackageMapping(files, meta, _) <- mappings
            if meta.config != "false"
            (file, name) <- files
            if file.isFile
          } yield name
          IO.writeLines(cfile, conffiles)
          chmod(cfile, "0644")
          cfile
      },
      debianMD5sumsFile <<= (debianExplodedPackage, target) map {
        (mappings, dir) =>
    val md5file = dir / Names.DebianMaintainerScripts / "md5sums"
          val md5sums = for {
            (file, name) <- (dir.*** --- dir pair relativeTo(dir))
            if file.isFile
      if !(name startsWith Names.DebianMaintainerScripts)
            if !(name contains "debian-binary")
            // TODO - detect symlinks with Java7 (when we can) rather than hackery...
            if file.getCanonicalPath == file.getAbsolutePath
            fixedName = if (name startsWith "/") name drop 1 else name
          } yield Hashing.md5Sum(file) + "  " + fixedName
          IO.writeLines(md5file, md5sums)
          chmod(md5file, "0644")
          md5file
      },
      debianMakeChownReplacements <<= (linuxPackageMappings, streams) map makeChownReplacements,
      debianExplodedPackage <<= (linuxPackageMappings, debianControlFile, debianMaintainerScripts, debianConffilesFile, debianChangelog,
        linuxScriptReplacements, debianMakeChownReplacements, linuxPackageSymlinks, target, streams)
  map { (mappings, _, maintScripts, _, changelog, replacements, chown, symlinks, t, streams) =>

          // Create files and directories
          mappings foreach {
            case LinuxPackageMapping(paths, perms, zipped) =>
              val (dirs, files) = paths.partition(_._1.isDirectory)
              dirs map {
                case (_, name) => t / name
              } foreach { targetDir =>
                targetDir mkdirs ()
                chmod(targetDir, perms.permissions)
              }

              files map {
                case (file, name) => (file, t / name)
              } foreach {
                case (source, target) => copyAndFixPerms(source, target, perms, zipped)
              }
          }
          // Now generate relative symlinks
          LinuxSymlink.makeSymLinks(symlinks, t, false)

          // Put the maintainer files in `dir / "DEBIAN"` named as specified.
          // Valid values for the name are preinst,postinst,prerm,postrm
          for ((file, name) <- maintScripts) {
      val targetFile = t / Names.DebianMaintainerScripts / name
            copyAndFixPerms(file, targetFile, LinuxFileMetaData())
            filterAndFixPerms(targetFile, chown +: replacements, LinuxFileMetaData())
          }
          t
        },
      // Replacement for ${{header}} as debian control scripts are bash scripts
      linuxScriptReplacements += ("header" -> "#!/bin/sh\nset -e")

    // Adding package specific implementation settings
    )
  )

}

/**
  * == Debian Helper Methods ==
  *
  * This trait provides a set of helper methods for debian packaging
  * implementations.
  *
  * Most of the methods are for java 6 file permission handling and
  * debian script adjustements.
  *
  */
trait DebianPluginLike {

  /** validate group and usernames for debian systems */
  val UserNamePattern = "^[a-z][-a-z0-9_]*$".r

  private[debian] final def generateDebianMaintainerScripts(
    scripts: Map[String, Seq[String]],
    replacements: Seq[(String, String)],
    tmpDir: File): Seq[(File, String)] = {

    scripts.map {
      case (scriptName, content) =>
  val scriptBits = TemplateWriter.generateScriptFromLines(content, replacements)
  val script = tmpDir / "tmp" / "debian" / scriptName
  IO.write(script, scriptBits mkString "\n")
  script -> scriptName
    }.toList
  }

  private[debian] final def defaultMaintainerScript(name: String): Option[List[String]] = {
    val url = Option(getClass getResource s"$name-template")
    url.map(source => IO.readLinesURL(source))
  }

  private[debian] final def copyAndFixPerms(from: File, to: File, perms: LinuxFileMetaData, zipped: Boolean = false): Unit = {
    if (zipped) {
      IO.withTemporaryDirectory { dir =>
        val tmp = dir / from.getName
        IO.copyFile(from, tmp)
        val zipped = Archives.gzip(tmp)
        IO.copyFile(zipped, to, true)
      }
    } else IO.copyFile(from, to, true)
    // If we have a directory, we need to alter the perms.
    chmod(to, perms.permissions)
    // TODO - Can we do anything about user/group ownership?
  }

  private[debian] final def filterAndFixPerms(script: File, replacements: Seq[(String, String)], perms: LinuxFileMetaData): File = {
    val filtered = TemplateWriter.generateScript(script.toURI.toURL, replacements)
    IO.delete(script)
    IO.write(script, filtered)
    chmod(script, perms.permissions)
    script
  }

  private[debian] final def prependAndFixPerms(script: File, lines: Seq[String], perms: LinuxFileMetaData): File = {
    val old = IO.readLines(script)
    IO.writeLines(script, lines ++ old, append = false)
    chmod(script, perms.permissions)
    script
  }

  private[debian] final def appendAndFixPerms(script: File, lines: Seq[String], perms: LinuxFileMetaData): File = {
    IO.writeLines(script, lines, append = true)
    chmod(script, perms.permissions)
    script
  }

  private[debian] final def createFileIfRequired(script: File, perms: LinuxFileMetaData): File = {
    if (!script.exists()) {
      script.createNewFile()
      chmod(script, perms.permissions)
    }
    script
  }

  private[debian] final def validateUserGroupNames(user: String, streams: TaskStreams) {
    if ((UserNamePattern findFirstIn user).isEmpty) {
      streams.log.warn("The user or group '" + user + "' may contain invalid characters for Debian based distributions")
    }
    if (user.length > 32) {
      streams.log.warn("The length of '" + user + "' must be not be greater than 32 characters for Debian based distributions.")
    }
  }

  @deprecated("Will be removed", "1.0.3")
  private[debian] def scriptMapping(scriptName: String, script: Option[File], controlDir: File): Option[(File, String)] = {
    (script, controlDir) match {
      // check if user defined script exists
      case (_, dir) if (dir / scriptName).exists =>
  Some(file((dir / scriptName).getAbsolutePath) -> scriptName)
      // create mappings for generated script
      case (scr, _) => scr.map(_ -> scriptName)
    }
  }

  /**
    * Debian assumes the application chowns the necessary files and directories in the
    * control scripts (Pre/Postinst).
    *
    * This method generates a replacement which can be inserted in bash script to chown
    * all files which are not root. While adding the chown commands it checks if the users
    * and groups have valid names.
    *
    * @param mappings - all mapped files
    * @param streams - logging
    * @return (CHOWN_REPLACEMENT -> ".. list of chown commands")
    */
  private[debian] def makeChownReplacements(mappings: Seq[LinuxPackageMapping], streams: TaskStreams): (String, String) = {
    // how to create the chownCmd. TODO maybe configurable?
    def chownCmd(user: String, group: String)(path: String): String = s"chown $user:$group $path"

    val header = "# Chown definitions created by SBT Native Packager\n"
    // Check for non root user/group and create chown commands
    // filter all root mappings, map to (user,group) key, group by, append everything
    val chowns = mappings filter {
      case LinuxPackageMapping(_, LinuxFileMetaData(Users.Root, Users.Root, _, _, _), _) => false
      case _ => true
    } map {
      case LinuxPackageMapping(paths, meta, _) => (meta.user, meta.group) -> paths
    } groupBy (_._1) map {
      case ((user, group), pathList) =>
        validateUserGroupNames(user, streams)
        validateUserGroupNames(group, streams)
        val chown = chownCmd(user, group) _
        // remove key, flatten it and then use mapping path (_.2) to create chown command
        pathList.map(_._2).flatten map (m => chown(m._2))
    }
    val replacement = header :: chowns.flatten.toList mkString "\n"
    DebianPlugin.CHOWN_REPLACEMENT -> replacement
  }

  private[debian] def archiveFilename(appName: String, version: String, arch: String): String = {
    appName + "_" + version + "_" + arch + ".deb"
  }

  private[debian] def changesFilename(appName: String, version: String, arch: String): String = {
    appName + "_" + version + "_" + arch + ".changes"
  }
}

object DebianDeployPlugin extends AutoPlugin {

  import DebianPlugin.autoImport._

  override def requires = DebianPlugin

  override def projectSettings =
    SettingsHelper.makeDeploymentSettings(Debian, packageBin in Debian, "deb") ++
      SettingsHelper.makeDeploymentSettings(Debian, genChanges in Debian, "changes")
}
