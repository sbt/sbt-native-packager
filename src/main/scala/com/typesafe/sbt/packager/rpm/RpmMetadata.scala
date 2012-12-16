package com.typesafe.sbt
package packager
package rpm

import linux.{LinuxPackageMapping,LinuxFileMetaData}
import sbt._

case class RpmMetadata(
    name: String,
    version: String,
    release: String,
    arch: String,
    vendor: String,
    os: String,
    summary: String,
    description: String) {
}

/** 
 * The Description used to generate an RPM
 */
case class RpmDescription(
    license: Option[String] = None,
    distribution: Option[String] = None,
    //vendor: Option[String] = None,
    url: Option[String] = None,
    group: Option[String] = None,
    packager: Option[String] = None,
    icon: Option[String] = None
    )
    
case class RpmDependencies(
    provides: Seq[String] = Seq.empty,
    requirements: Seq[String] = Seq.empty,
    prereq: Seq[String] = Seq.empty,
    obsoletes: Seq[String] = Seq.empty,
    conflicts: Seq[String] = Seq.empty) {
  def contents: String = {
    val sb = new StringBuilder
    def appendSetting(prefix: String, values: Seq[String]) =
      values foreach (v => sb append (prefix + v + "\n"))
    appendSetting("Provides: ", provides)
    appendSetting("Requires: ", requirements)
    appendSetting("PreReq: ", prereq)
    appendSetting("Obsoletes: ", obsoletes)
    appendSetting("Conflicts: ", conflicts)
    sb.toString
  }
}

case class RpmScripts(
    pretrans: Option[String] = None,
    pre: Option[String] = None,
    post: Option[String] = None,
    verifyscript: Option[String] = None,
    posttrans: Option[String] = None,
    preun: Option[String] = None,
    postun: Option[String] = None
    ) {
    def contents(): String = {
        val labelledScripts = Seq("%pretrans","%pre","%post","%verifyscript","%posttrans","%preun","%postun")
                         .zip(Seq(  pretrans,   pre,   post,   verifyscript,   posttrans,   preun,   postun))
        labelledScripts.collect{case (a, Some(b))  => a + "\n" + b} .mkString("\n\n")
    }

}

case class RpmSpec(meta: RpmMetadata,
    desc: RpmDescription = RpmDescription(),
    deps: RpmDependencies = RpmDependencies(),
    scriptlets: RpmScripts = RpmScripts(),
    mappings: Seq[LinuxPackageMapping] = Seq.empty) {
  
  private[this] def makeFilesLine(target: String, meta: LinuxFileMetaData, isDir: Boolean): String = {
    val sb = new StringBuilder
    meta.config.toLowerCase match {
      case "false" => ()
      case "true"  => sb append "%config "
      case x       => sb append ("%config("+x+") ")
    }
    if(meta.docs) sb append "%doc "
    if(isDir) sb append "%dir "
    // TODO - map dirs...
    sb append "%attr("
    sb append meta.permissions
    sb append ','
    sb append meta.user
    sb append ','
    sb append meta.group
    sb append ") "
    sb append (target.contains(' ') match {
      case true => "\"%s\"" format target
      case false => target
    })
    sb append '\n'
    sb.toString
  }
  
  private[this] def fileSection: String = {
    val sb = new StringBuilder
    sb append "\n%files\n"
    // TODO - default attribute string.
    for {
      mapping <- mappings
      (file, dest) <- mapping.mappings
    } sb append makeFilesLine(dest, mapping.fileData, file.isDirectory)
    sb.toString
  }
  
  private[this] def installSection(root: File): String = {
    val sb = new StringBuilder
    sb append "\n"
    sb append "%install\n"
    sb append "if [ -e \"$RPM_BUILD_ROOT\" ]; "
    sb append "then\n"
    sb append "  mv \""
    sb append root.getAbsolutePath
    sb append "\"/* \"$RPM_BUILD_ROOT\"\n"
    sb append "else\n"
    sb append "  mv \""
    sb append root.getAbsolutePath
    sb append "\" \"$RPM_BUILD_ROOT\"\n"
    sb append "fi\n"
    sb.toString
  }
  
  // TODO - This is *very* tied to RPM helper, may belong *in* RpmHelper
  def writeSpec(rpmRoot: File, tmpRoot: File): String = {
    val sb = new StringBuilder
    sb append ("Name: %s\n" format meta.name)
    sb append ("Version: %s\n" format meta.version)
    sb append ("Release: %s\n" format meta.release)    
    sb append ("Summary: %s\n" format meta.summary)
    
    desc.license foreach { v => sb append ("License: %s\n" format v)}
    desc.distribution foreach { v => sb append ("Distribution: %s\n" format v)}
    // TODO - Icon
    
    sb append ("Vendor: %s\n" format meta.vendor)
    desc.url foreach { v => sb append ("URL: %s\n" format v)}
    desc.group foreach { v => sb append ("Group: %s\n" format v)}
    desc.packager foreach { v => sb append ("Packager: %s\n" format v)}
    
    sb append deps.contents
    
    // TODO - autoprov + autoreq
    
    sb append ("BuildRoot: %s\n\n" format rpmRoot.getAbsolutePath)
    
    sb append "%description\n"
    sb append meta.description
    sb append "\n\n"

    // write build as moving everything into RPM directory.
    sb append installSection(tmpRoot)
    // TODO - Allow symlinks

    // write scriptlets
    sb append scriptlets.contents()

    // Write file mappings
    sb append fileSection
    // TODO - Write triggers...
    // TODO - Write changelog...
    
    sb.toString
  }
}
