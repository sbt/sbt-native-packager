package com.typesafe.sbt
package packager
package debian

import Keys._
import sbt._
import sbt.Keys.{ target, name, normalizedName, TaskStreams }
import linux.{ LinuxFileMetaData, LinuxPackageMapping, LinuxSymlink }
import linux.Keys.{ linuxScriptReplacements, daemonShell }
import com.typesafe.sbt.packager.Hashing
import com.typesafe.sbt.packager.archetypes.TemplateWriter

trait DebianPlugin extends Plugin with linux.LinuxPlugin with NativePackaging with JDebPackaging {
  val Debian = config("debian") extend Linux
  val UserNamePattern = "^[a-z][-a-z0-9_]*$".r

  import com.typesafe.sbt.packager.universal.Archives
  import DebianPlugin.Names
  import linux.LinuxPlugin.Users

  def debianSettings: Seq[Setting[_]] = Seq(
    debianPriority := "optional",
    debianSection := "java",
    debianPackageDependencies := Seq.empty,
    debianPackageRecommends := Seq.empty,
    debianSignRole := "builder",
    target in Debian <<= (target, name in Debian, version in Debian) apply ((t, n, v) => t / (n + "-" + v)),
    name in Debian <<= (name in Linux),
    version in Debian <<= (version in Linux),
    linuxPackageMappings in Debian <<= linuxPackageMappings,
    packageDescription in Debian <<= packageDescription in Linux,
    packageSummary in Debian <<= packageSummary in Linux,
    maintainer in Debian <<= maintainer in Linux,

    debianControlScriptsDirectory <<= (sourceDirectory) apply (_ / "debian" / Names.Debian),
    debianMaintainerScripts := Seq.empty,
    debianMakePreinstScript := None,
    debianMakePrermScript := None,
    debianMakePostinstScript := None,
    debianMakePostrmScript := None,
    debianChangelog := None,

    debianMaintainerScripts <++= (debianMakePrermScript, debianControlScriptsDirectory) map scriptMapping(Names.Prerm),
    debianMaintainerScripts <++= (debianMakePreinstScript, debianControlScriptsDirectory) map scriptMapping(Names.Preinst),
    debianMaintainerScripts <++= (debianMakePostinstScript, debianControlScriptsDirectory) map scriptMapping(Names.Postinst),
    debianMaintainerScripts <++= (debianMakePostrmScript, debianControlScriptsDirectory) map scriptMapping(Names.Postrm)) ++
    inConfig(Debian)(Seq(
      packageArchitecture := "all",
      debianPackageInfo <<=
        (normalizedName, version, maintainer, packageSummary, packageDescription) apply PackageInfo,
      debianPackageMetadata <<=
        (debianPackageInfo,
          debianPriority, packageArchitecture, debianSection,
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
                 packageDescription in Debian := "My package Description"""")
          }
          val cfile = dir / Names.Debian / Names.Control
          IO.write(cfile, data.makeContent(size), java.nio.charset.Charset.defaultCharset)
          chmod(cfile, "0644")
          cfile
      },
      debianConffilesFile <<= (linuxPackageMappings, target) map {
        (mappings, dir) =>
          val cfile = dir / Names.Debian / Names.Conffiles
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
          val md5file = dir / Names.Debian / "md5sums"
          val md5sums = for {
            (file, name) <- (dir.*** --- dir x relativeTo(dir))
            if file.isFile
            if !(name startsWith Names.Debian)
            if !(name contains "debian-binary")
            // TODO - detect symlinks with Java7 (when we can) rather than hackery...
            if file.getCanonicalPath == file.getAbsolutePath
            fixedName = if (name startsWith "/") name drop 1 else name
          } yield Hashing.md5Sum(file) + "  " + fixedName
          IO.writeLines(md5file, md5sums)
          chmod(md5file, "0644")
          md5file
      },
      // Setting the packaging strategy
      packageBin <<= Native()
    ) ++ debianNativeSettings

    )

  private[this] def scriptMapping(scriptName: String)(script: Option[File], controlDir: File): Seq[(File, String)] = {
    (script, controlDir) match {
      // check if user defined script exists
      case (_, dir) if (dir / scriptName).exists =>
        Seq(file((dir / scriptName).getAbsolutePath) -> scriptName)
      // create mappings for generated script
      case (scr, _) => scr.toSeq.map(_ -> scriptName)
    }
  }
}

object DebianPlugin {
  object Names {
    val DebianSource = "debian"
    val Debian = "DEBIAN"

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

}
