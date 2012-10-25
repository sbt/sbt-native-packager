package com.typesafe.sbt
package packager
package linux

import Keys._
import sbt._

/** Plugin trait containing all the generic values used for
 * packaging linux software.
 */
trait LinuxPlugin extends Plugin{
  // TODO - is this needed
  val Linux = config("linux")
  
  def linuxSettings: Seq[Setting[_]] = Seq(
    linuxPackageMappings := Seq.empty,
    sourceDirectory in Linux <<= sourceDirectory apply (_ / "linux"),
    generateManPages <<= (sourceDirectory in Linux, sbt.Keys.streams) map { (dir, s) =>
      for( file <- (dir / "usr/share/man/man1" ** "*.1").get ) {
        val man = makeMan(file)
        s.log.info("Generated man page for[" + file + "] =")
        s.log.info(man)
      }  
    },
    packageSummary in Linux := "",
    packageDescription in Linux := ""
  )
  
  /** DSL for packaging files into .deb */
  def packageMapping(files: (File, String)*) = LinuxPackageMapping(files)
  
  
  /** Create a ascii friendly string for a man page. */  
  final def makeMan(file: File): String = 
    Process("groff -man -Tascii " + file.getAbsolutePath).!!
}