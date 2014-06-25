package com.typesafe.sbt
package packager
package debian

import Keys._
import sbt._
import sbt.Keys.{ target, name, normalizedName, TaskStreams }
import linux.{ LinuxFileMetaData, LinuxPackageMapping, LinuxSymlink }
import linux.Keys.{ linuxScriptReplacements, daemonShell }

trait JDebPackaging { this: DebianPlugin with linux.LinuxPlugin =>

  private[debian] def debianJDebSettings: Seq[Setting[_]] = Seq.empty
}

/**
 * This provides the task for building a debian packaging with
 * the java-based implementation jdeb
 */
object JDeb {

  // https://github.com/tcurdt/jdeb/blob/master/src/main/java/org/vafer/jdeb/maven/DebMojo.java#L503
  def apply(): Def.Initialize[Task[java.io.File]] = ???
}