package com.typesafe.sbt
package packager
package rpm

import sbt._
import com.typesafe.sbt.packager.linux.{LinuxFileMetaData, LinuxPackageMapping, LinuxPlugin, LinuxSymlink}
import com.typesafe.sbt.packager.rpm.RpmPlugin.Names._
import com.typesafe.sbt.packager.archetypes.TemplateWriter
import java.io.File

case class RpmMetadata(
  name: String,
  version: String,
  release: String,
  prefix: Option[String] = None,
  arch: String,
  vendor: String,
  os: String,
  summary: String,
  description: String,
  autoprov: String,
  autoreq: String,
  epoch: Option[Int]
)

/**
  * The Description used to generate an RPM
  */
case class RpmDescription(
  license: Option[String] = None,
  distribution: Option[String] = None,
  url: Option[String] = None,
  group: Option[String] = None,
  packager: Option[String] = None,
  icon: Option[String] = None,
  changelogFile: Option[String] = None
)

case class RpmDependencies(
  provides: Seq[String] = Seq.empty,
  requirements: Seq[String] = Seq.empty,
  prereq: Seq[String] = Seq.empty,
  obsoletes: Seq[String] = Seq.empty,
  conflicts: Seq[String] = Seq.empty
) {
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

/**
  * Parameters stay because of binary compatibility.
  */
case class RpmScripts(
  pretrans: Option[String] = None,
  pre: Option[String] = None,
  post: Option[String] = None,
  verifyscript: Option[String] = None,
  posttrans: Option[String] = None,
  preun: Option[String] = None,
  postun: Option[String] = None
) {

  def pretransContent(): String = buildScript("pretrans", pretrans)
  def preContent(): String = buildScript("pre", pre)
  def postContent(): String = buildScript("post", post)
  def posttransContent(): String = buildScript("posttrans", posttrans)
  def verifyscriptContent(): String = buildScript("verifyscript", verifyscript)
  def preunContent(): String = buildScript("preun", preun)
  def postunContent(): String = buildScript("postun", postun)

  private def buildScript(name: String, script: Option[String]): String =
    script.map { code =>
      s"""
         |%$name
         |$code
         |
         |""".stripMargin
    } getOrElse ""

  @deprecated(
    "Call individual scriptlet content method instead, e.g. pretransContent(). This is to allow managing symlink during %post and %postun so it can be relocated",
    since = "1.0.5-M4"
  )
  @deprecated("Use contents(maintainerScripts) until next major release", "1.1.x")
  def contents(): String = {
    val labelledScripts = Seq("%pretrans", "%pre", "%post", "%verifyscript", "%posttrans", "%preun", "%postun").zip(
      Seq(pretrans, pre, post, verifyscript, posttrans, preun, postun)
    )
    labelledScripts.collect { case (a, Some(b)) => a + "\n" + b }.mkString("\n\n")
  }

}

object RpmScripts {

  def fromMaintainerScripts(
    maintainerScripts: Map[String, Seq[String]],
    replacements: Seq[(String, String)]
  ): RpmScripts = {
    val toContent = toContentWith(replacements) _
    RpmScripts(
      pretrans = maintainerScripts.get(Pretrans).map(toContent),
      pre = maintainerScripts.get(Pre).map(toContent),
      post = maintainerScripts.get(Post).map(toContent),
      verifyscript = maintainerScripts.get(Verifyscript).map(toContent),
      posttrans = maintainerScripts.get(Posttrans).map(toContent),
      preun = maintainerScripts.get(Preun).map(toContent),
      postun = maintainerScripts.get(Postun).map(toContent)
    )
  }

  // insert replacements
  private def toContentWith(replacements: Seq[(String, String)])(lines: Seq[String]): String =
    TemplateWriter.generateScriptFromLines(lines, replacements).mkString("\n")

}

case class RpmSpec(
  meta: RpmMetadata,
  desc: RpmDescription = RpmDescription(),
  deps: RpmDependencies = RpmDependencies(),
  setarch: Option[String],
  scriptlets: RpmScripts = RpmScripts(),
  mappings: Seq[LinuxPackageMapping] = Seq.empty,
  symlinks: Seq[LinuxSymlink] = Seq.empty,
  installLocation: String
) {

  def installDir: String =
    LinuxPlugin.chdir(installLocation, meta.name)

  // TODO - here we want to validate that all the data we have is ok to place
  // in the RPM.  e.g. the Description/vendor etc. must meet specific requirements.
  // For now we just check existence.
  def validate(log: Logger): Unit = {
    def ensureOr[T](value: T, msg: String, validator: T => Boolean): Boolean =
      if (validator(value)) true
      else {
        log.error(msg)
        false
      }
    def isNonEmpty(s: String): Boolean = !s.isEmpty
    // format: off
    val emptyValidators =
      Seq(
        ensureOr(meta.name, "`name in Rpm` is empty.  Please provide one.", isNonEmpty),
        ensureOr(meta.version, "`version in Rpm` is empty.  Please provide a valid version for the rpm SPEC.", isNonEmpty),
        ensureOr(meta.release, "`rpmRelease` is empty.  Please provide a valid release number for the rpm SPEC.", isNonEmpty),
        ensureOr(meta.arch, "`packageArchitecture in Rpm` is empty.  Please provide a valid architecture for the rpm SPEC.", isNonEmpty),
        ensureOr(meta.vendor, "`rpmVendor` is empty.  Please provide a valid vendor for the rpm SPEC.", isNonEmpty),
        ensureOr(meta.os, "`rpmOs` is empty.  Please provide a valid os value for the rpm SPEC.", isNonEmpty),
        ensureOr(meta.summary, "`packageSummary in Rpm` is empty.  Please provide a valid summary for the rpm SPEC.", isNonEmpty),
        ensureOr(meta.description, "`packageDescription in Rpm` is empty.  Please provide a valid description for the rpm SPEC.", isNonEmpty)
      )
    // format: on
    // TODO - Continue validating after this point?
    if (!emptyValidators.forall(identity))
      sys.error("There are issues with the rpm spec data.")
  }

  private[this] def fixFilename(n: String): String = {
    val tmp =
      if (n startsWith "/") n
      else "/" + n
    if (tmp.contains(' ')) "\"%s\"" format tmp
    else tmp
  }

  private[this] def makeFilesLine(target: String, meta: LinuxFileMetaData, isDir: Boolean): String = {

    val sb = new StringBuilder
    meta.config.toLowerCase match {
      case "false" => ()
      case "true"  => sb append "%config "
      case x       => sb append ("%config(" + x + ") ")
    }
    if (meta.docs) sb append "%doc "
    if (isDir) sb append "%dir "
    // TODO - map dirs...
    sb append "%attr("
    sb append meta.permissions
    sb append ','
    sb append meta.user
    sb append ','
    sb append meta.group
    sb append ") "
    sb append fixFilename(target)
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

    symlinks foreach (l => sb append s"${l.link}\n")
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

    meta.epoch filter (_ >= 0) foreach { epoch =>
      sb append ("Epoch: %d\n" format epoch)
    }

    meta.prefix foreach { v =>
      sb append ("prefix: %s\n" format v)
    }

    desc.license foreach { v =>
      sb append ("License: %s\n" format v)
    }
    desc.distribution foreach { v =>
      sb append ("Distribution: %s\n" format v)
    }
    // TODO - Icon

    sb append ("Vendor: %s\n" format meta.vendor)
    desc.url foreach { v =>
      sb append ("URL: %s\n" format v)
    }
    desc.group foreach { v =>
      sb append ("Group: %s\n" format v)
    }
    desc.packager foreach { v =>
      sb append ("Packager: %s\n" format v)
    }

    sb append deps.contents

    sb append ("AutoProv: %s\n" format meta.autoprov)
    sb append ("AutoReq: %s\n" format meta.autoreq)

    sb append ("BuildRoot: %s\n" format rpmRoot.getAbsolutePath)
    sb append ("BuildArch: %s\n\n" format meta.arch)

    sb append "%description\n"
    sb append meta.description
    sb append "\n\n"

    // write build as moving everything into RPM directory.
    sb append installSection(tmpRoot)
    // TODO - Allow symlinks

    // write scriptlets
    sb append scriptlets.pretransContent()
    sb append scriptlets.preContent()
    sb append scriptlets.postContent()
    sb append scriptlets.verifyscriptContent()
    sb append scriptlets.posttransContent()
    sb append scriptlets.preunContent()
    sb append scriptlets.postunContent()

    // Write file mappings
    sb append fileSection
    // TODO - Write triggers...
    desc.changelogFile foreach { changelog =>
      val tmpFile = new File(changelog)
      if (tmpFile.isFile() && tmpFile.exists()) {
        // if (Files.exists(Paths.get(changelog))) {
        val content = scala.io.Source.fromFile(changelog).mkString
        sb append "%changelog\n"
        sb append ("%s\n" format content)
      }
    }
    sb.toString
  }
}
