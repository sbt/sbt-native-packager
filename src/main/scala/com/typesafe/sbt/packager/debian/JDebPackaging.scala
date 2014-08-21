package com.typesafe.sbt
package packager
package debian

import Keys._
import sbt._
import sbt.Keys.{ target, normalizedName }
import linux.{ LinuxSymlink }
import com.typesafe.sbt.packager.linux.LinuxPackageMapping
import scala.collection.JavaConversions._

import org.vafer.jdeb.{ DebMaker, DataProducer }
import org.vafer.jdeb.mapping._
import org.vafer.jdeb.producers._
import DebianPlugin.Names

/**
 * This provides a java based debian packaging implementation based
 * on the jdeb maven-plugin. To use this, put this into your build.sbt
 *
 * {{
 *    packageBin in Debian <<= debianJDebPackaging in Debian
 * }}
 *
 * @author Nepomuk Seiler
 * @see https://github.com/tcurdt/jdeb/blob/master/src/main/java/org/vafer/jdeb/maven/DebMojo.java#L503
 *
 */
trait JDebPackaging { this: DebianPlugin with linux.LinuxPlugin =>

  private[debian] def debianJDebSettings: Seq[Setting[_]] = Seq(

    /**
     * Depends on the 'debianExplodedPackage' task as this creates all the files
     * which are defined in the mappings.
     */
    debianJDebPackaging <<= (debianExplodedPackage, linuxPackageMappings, linuxPackageSymlinks,
      debianControlFile, debianMaintainerScripts, debianConffilesFile,
      normalizedName, version, packageArchitecture, target, streams) map {
        (_, mappings, symlinks, controlfile, controlscripts, conffile,
        name, version, arch, target, s) =>
          s.log.info("Building debian package with java based implementation 'jdeb'")
          val console = new JDebConsole(s.log)
          val archive = archiveFilename(name, version, arch)
          val debianFile = target.getParentFile / archive
          val debMaker = new DebMaker(console,
            fileAndDirectoryProducers(mappings, target) ++ linkProducers(symlinks),
            conffileProducers()
          )
          debMaker setDeb debianFile
          debMaker setControl (target / Names.Debian)

          // TODO set compression, gzip is default
          // TODO add signing with setKeyring, setKey, setPassphrase, setSignPackage, setSignMethod, setSignRole
          debMaker validate ()
          debMaker makeDeb ()
          debianFile
      })

  /**
   * Creating file and directory producers. These "produce" the
   * files for the debian packaging
   */
  private[debian] def fileAndDirectoryProducers(mappings: Seq[LinuxPackageMapping], target: File): Seq[DataProducer] = mappings.map {
    case LinuxPackageMapping(paths, perms, zipped) =>
      paths map {
        case (path, name) if path.isDirectory =>
          val permMapper = new PermMapper(-1, -1, perms.user, perms.group, null, perms.permissions, -1, null)
          val dirName = if (name.startsWith("/")) name.drop(1) else name
          new DataProducerDirectory(target, Array(dirName), null, Array(permMapper))
        case (path, name) =>
          val permMapper = new PermMapper(-1, -1, perms.user, perms.group, perms.permissions, null, -1, null)
          new DataProducerFile(target / name, name, null, null, Array(permMapper))
      }
  }.flatten

  /**
   * Creating link producers for symlinks.
   */
  private[debian] def linkProducers(symlinks: Seq[LinuxSymlink]): Seq[DataProducer] = symlinks map {
    case LinuxSymlink(link, destination) =>
      new DataProducerLink(link, destination, true, null, null, null)
  }

  /**
   * Creating the files which should be added as conffiles.
   * This is currently handled by the debian plugin itself.
   */
  private[debian] def conffileProducers(): Seq[DataProducer] = Seq.empty

}

/**
 * This provides the task for building a debian packaging with
 * the java-based implementation jdeb
 */
class JDebConsole(log: Logger) extends org.vafer.jdeb.Console {

  def debug(message: String) = log debug message

  def info(message: String) = log info message

  def warn(message: String) = log warn message
}
