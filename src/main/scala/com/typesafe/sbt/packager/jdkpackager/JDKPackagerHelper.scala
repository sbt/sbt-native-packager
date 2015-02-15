package com.typesafe.sbt.packager.jdkpackager

import com.typesafe.sbt.packager.chmod
import sbt._

/**
 * Support/helper functions for interacting with the `javapackager` tool.
 * @author <a href="mailto:fitch@datamininglab.com">Simeon H.K. Fitch</a>
 * @since 2/11/15
 */
object JDKPackagerHelper {

  // Try to compute determine a default path for the java packager tool.
  def locateJDKPackagerTool(): Option[File] = {
    val jdkHome = sys.props.get("java.home").map(p ⇒ file(p))

    // TODO: How to get version of javaHome target?
    val javaVersion = VersionNumber(sys.props("java.specification.version"))
    val toolname = javaVersion.numbers.take(2) match {
      case Seq(1, m) if m >= 8 ⇒ "javapackager"
      case _                   ⇒ sys.error("Need at least JDK version 1.8 to run JDKPackager plugin")
    }
    jdkHome
      .map(f ⇒ if (f.getName == "jre") f / ".." else f)
      .map(f ⇒ f / "bin" / toolname)
      .filter(_.exists())
  }

  def mkProcess(
    tool: File,
    mode: String,
    argMap: Map[String, String],
    log: Logger) = {

    val invocation = Seq(tool.getAbsolutePath, mode, "-v")

    val argSeq = argMap.map(p ⇒ Seq(p._1, p._2)).flatten[String].filter(_.length > 0)
    val args = invocation ++ argSeq

    val argString = args.map {
      case s if s.contains(" ") ⇒ s""""$s""""
      case s                    ⇒ s
    }.mkString(" ")
    log.debug(s"Package command: $argString")

    // To help debug arguments
    val script = file(argMap("-outdir")) / "jdkpackager.sh"
    IO.write(script, s"#!/bin/bash\n$argString\n")
    chmod(script, "766")

    Process(args)
  }

  def mkArgMap(
    name: String,
    version: String,
    description: String,
    maintainer: String,
    packageType: String,
    mainJar: String,
    mainClass: Option[String],
    basename: String,
    iconPath: Option[File],
    outputDir: File,
    sourceDir: File) = {

    def iconArg = iconPath
      .map(_.getAbsolutePath)
      .map(p ⇒ Map(s"-Bicon=$p" -> ""))
      .getOrElse(Map.empty)

    def mainClassArg = mainClass
      .map(c ⇒ Map("-appclass" -> c))
      .getOrElse(Map.empty)

    //    val cpSep = sys.props("path.separator")
    //    val cp = classpath.map(p ⇒ "lib/" + p)
    //    val cpStr = cp.mkString(cpSep)

    // See http://docs.oracle.com/javase/8/docs/technotes/tools/unix/javapackager.html and
    // http://docs.oracle.com/javase/8/docs/technotes/tools/windows/javapackager.html for
    // command line options. NB: Built-in `-help` is incomplete.
    Map(
      "-name" -> name,
      "-srcdir" -> sourceDir.getAbsolutePath,
      "-native" -> packageType,
      "-outdir" -> outputDir.getAbsolutePath,
      "-outfile" -> basename,
      "-description" -> description,
      "-vendor" -> maintainer,
      s"-BappVersion=$version" -> "",
      s"-BmainJar=lib/$mainJar" -> ""
    ) ++ mainClassArg ++ iconArg

    // TODO:
    // * copyright/license
    // * JVM options
    // * application arguments
    // * environment variables?
  }
}
