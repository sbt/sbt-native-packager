package com.typesafe.sbt.packager
package debian

import com.typesafe.sbt.packager.Compat.*
import com.typesafe.sbt.packager.PluginCompat
import com.typesafe.sbt.packager.archetypes.TemplateWriter
import com.typesafe.sbt.packager.universal.Archives
import sbt.{*, given}
import sbt.Keys.{classpathTypes, fileConverter, normalizedName, packageBin, streams, target, version}
import com.typesafe.sbt.packager.linux.{LinuxFileMetaData, LinuxPackageMapping, LinuxSymlink}
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.{
  linuxPackageMappings,
  linuxPackageSymlinks,
  linuxScriptReplacements,
  packageArchitecture
}
import scala.collection.JavaConverters._
import DebianPlugin.Names
import DebianPlugin.autoImport._
import xsbti.FileConverter

/**
  * ==JDeb Plugin==
  * This provides a java based debian packaging implementation based on the jdeb maven-plugin. To use this, put this
  * into your build.sbt
  *
  * @example
  *   Enable the plugin in the `build.sbt`
  *   {{{
  *  enablePlugins(JDebPackaging)
  *   }}}
  *
  * @author
  *   Nepomuk Seiler
  * @see
  *   [[https://github.com/tcurdt/jdeb/blob/master/src/main/java/org/vafer/jdeb/maven/DebMojo.java#L503]]
  */
object JDebPackaging extends AutoPlugin with DebianPluginLike {

  override def requires: Plugins = DebianPlugin

  override lazy val projectSettings: Seq[Setting[?]] = inConfig(Debian)(jdebSettings)

  def jdebSettings =
    Seq(
      // FIXME do nothing. Java7 posix needed
      debianConffilesFile :=
        target.value / Names.DebianMaintainerScripts / Names.Conffiles,
      // FIXME copied from the debian plugin. Java7 posix needed
      debianControlFile := {
        val data = debianPackageMetadata.value
        val size = debianPackageInstallSize.value
        if (data.info.description == null || data.info.description.isEmpty)
          sys.error("""Debian / packageDescription cannot be empty. Use
                 Debian / packageDescription := "My package Description"""")
        val cfile = target.value / Names.DebianMaintainerScripts / Names.Control
        IO.write(cfile, data.makeContent(size), java.nio.charset.Charset.defaultCharset)
        cfile
      },
      /**
        * Depends on the 'debianExplodedPackage' task as this creates all the files which are defined in the mappings.
        */
      packageBin := Def.uncached {
        val conv0 = fileConverter.value
        implicit val conv: FileConverter = conv0
        val targetDir = target.value
        val log = streams.value.log
        val mappings = linuxPackageMappings.value
        val symlinks = linuxPackageSymlinks.value

        // unused, but needed as dependency
        val controlDir = targetDir / Names.DebianMaintainerScripts
        val _ = debianControlFile.value
        val conffile = debianConffilesFile.value
        val replacements = debianMakeChownReplacements.value +: linuxScriptReplacements.value

        val controlScripts = debianMaintainerScripts.value
        for ((file, name) <- controlScripts) {
          val targetFile = controlDir / name
          copyFiles(file, targetFile, LinuxFileMetaData())
          filterFiles(targetFile, replacements, LinuxFileMetaData())
        }

        log.info("Building debian package with java based implementation 'jdeb'")
        val archive = archiveFilename(normalizedName.value, version.value, packageArchitecture.value)
        val debianFile = targetDir.getParentFile / archive
        val debMaker = new JDebPackagingTask()
        debMaker.packageDebian(mappings, symlinks, debianFile, targetDir, fileConverter.value, log)
        PluginCompat.toFileRef(debianFile)
      },
      packageBin := Def.uncached((packageBin dependsOn debianControlFile).value),
      packageBin := Def.uncached((packageBin dependsOn debianConffilesFile).value),
      // workaround for sbt-coursier
      classpathTypes += "maven-plugin"
    )

  /**
    * The same as [[DebianPluginLike.copyAndFixPerms]] except chmod invocation (for windows compatibility). Permissions
    * will be handled by jDeb packager itself.
    */
  private def copyFiles(from: File, to: File, perms: LinuxFileMetaData, zipped: Boolean = false): Unit =
    if (zipped)
      IO.withTemporaryDirectory { dir =>
        val tmp = dir / from.getName
        IO.copyFile(from, tmp)
        val zipped = Archives.gzip(tmp)
        IO.copyFile(zipped, to, preserveLastModified = true)
      }
    else IO.copyFile(from, to, preserveLastModified = true)

  /**
    * The same as [[DebianPluginLike.filterAndFixPerms]] except chmod invocation (for windows compatibility).
    * Permissions will be handled by jDeb packager itself.
    */
  private final def filterFiles(script: File, replacements: Seq[(String, String)], perms: LinuxFileMetaData): File = {
    val filtered =
      TemplateWriter.generateScript(script.toURI.toURL, replacements)
    IO.delete(script)
    IO.write(script, filtered)
    script
  }

}

/**
  * This provides the task for building a debian packaging with the java-based implementation jdeb
  */
class JDebConsole(log: Logger) extends org.vafer.jdeb.Console {

  def debug(message: String): Unit = log debug message

  def info(message: String): Unit = log info message

  def warn(message: String): Unit = log warn message
}

/**
  * ==JDeb Packaging Task==
  *
  * This private class contains all the jdeb-plugin specific implementations. It's only invoked when the jdeb plugin is
  * enabled and the `Debian/packageBin` task is called. This means that all classes in `org.vafer.jdeb._` are only
  * loaded when required and allows us to put the dependency in the "provided" scope. The provided scope means that we
  * have less dependency issues in an sbt build.
  */
private class JDebPackagingTask {
  import org.vafer.jdeb.{DataProducer, DebMaker}
  import org.vafer.jdeb.mapping._
  import org.vafer.jdeb.producers._

  def packageDebian(
    mappings: Seq[LinuxPackageMapping],
    symlinks: Seq[LinuxSymlink],
    debianFile: File,
    targetDir: File,
    conv0: FileConverter,
    log: Logger
  ): Unit = {
    implicit val conv: FileConverter = conv0
    val debMaker = new DebMaker(
      new JDebConsole(log),
      (fileAndDirectoryProducers(mappings, targetDir) ++ linkProducers(symlinks)).asJava,
      conffileProducers(mappings, targetDir).asJava
    )
    // set compression default to none - in line with native version / allows rsync to be effective
    debMaker setCompression "none"
    debMaker setDepends ""
    debMaker setDeb debianFile
    debMaker setControl (targetDir / Names.DebianMaintainerScripts)

    // TODO add signing with setKeyring, setKey, setPassphrase, setSignPackage, setSignMethod, setSignRole
    debMaker.validate()
    debMaker.makeDeb()
  }

  /**
    * Creating file and directory producers. These "produce" the files for the debian packaging.
    *
    * May create duplicates together with the conffileProducers. This will be an performance improvement (reducing IO)
    */
  private def fileAndDirectoryProducers(mappings: Seq[LinuxPackageMapping], target: File): Seq[DataProducer] =
    mappings.flatMap { case LinuxPackageMapping(paths, perms, zipped) =>
      paths.map {
        // Directories need to be created so jdeb can pick them up
        case (path, name) if path.isDirectory =>
          val permMapper = new PermMapper(-1, -1, perms.user, perms.group, null, perms.permissions, -1, null)
          (target / cleanPath(name)).mkdirs()
          new DataProducerDirectory(target, Array(cleanPath(name)), null, Array(permMapper))

        // Files are just referenced
        case (path, name) =>
          new DataProducerFile(path, cleanPath(name), null, null, Array(filePermissions(perms)))
      }
    }

  /**
    * Creating link producers for symlinks.
    */
  private[debian] def linkProducers(symlinks: Seq[LinuxSymlink]): Seq[DataProducer] =
    symlinks map { case LinuxSymlink(link, destination) =>
      new DataProducerLink(link, destination, true, null, null, null)
    }

  /**
    * Creating the files which should be added as conffiles.
    */
  private[debian] def conffileProducers(linuxMappings: Seq[LinuxPackageMapping], target: File): Seq[DataProducer] = {

    val producers = linuxMappings.map {
      case LinuxPackageMapping(concretMappings, perms, _) if perms.config == "true" =>
        concretMappings collect {
          case (path, name) if path.isFile =>
            val permMapper = filePermissions(perms.withPerms("0644"))
            new DataProducerFile(path, cleanPath(name), null, null, Array(permMapper))
        }
      case _ => Seq.empty
    }

    producers.flatten
  }

  private[debian] def cleanPath(path: String): String =
    if (path startsWith "/") path drop 1 else path

  private[this] def filePermissions(perms: LinuxFileMetaData): PermMapper =
    new PermMapper(-1, -1, perms.user, perms.group, perms.permissions, null, -1, null)
}
