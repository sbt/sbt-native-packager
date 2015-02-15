package com.typesafe.sbt.packager.jdkpackager

import com.typesafe.sbt.packager.chmod
import sbt._

/**
 * Support/helper functions for interacting with the `javapackager` tool.
 * @author <a href="mailto:fitch@datamininglab.com">Simeon H.K. Fitch</a>
 * @since 2/11/15
 */
object JDKPackagerHelper {

  /** Attempts to compute the path to the `javapackager` tool. */
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

  /**
   * Generates key-value pairs to be converted into command line arguments fed to `javapackager`.
   * If an argument is mono/standalone (not key/value) then the key stores the complete argument
   * and the value is the empty string.
   */
  private[jdkpackager] def makeArgMap(
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
    sourceDir: File): Map[String, String] = {

    val iconArg = iconPath.toSeq
      .map(_.getAbsolutePath)
      .map(p ⇒ s"-Bicon=$p")

    val mainClassArg = mainClass
      .map(c ⇒ Map("-appclass" -> c))
      .getOrElse(Map.empty)

    // Make a setting?
    val jvmOptsFile = (sourceDir ** "jvmopts").getPaths.headOption.map(file)

    val jvmOptsArgs = jvmOptsFile.toSeq.flatMap { jvmopts ⇒
      IO.readLines(jvmopts).map {
        case a if a startsWith "-X" ⇒ s"-BjvmOptions=$a"
        case b if b startsWith "-D" ⇒ s"-BjvmProperties=${b.drop(2)}"
        case c                      ⇒ "" // Ignoring others.... is this OK?
      }.filter(_.nonEmpty)
    }

    // See http://docs.oracle.com/javase/8/docs/technotes/tools/unix/javapackager.html and
    // http://docs.oracle.com/javase/8/docs/technotes/tools/windows/javapackager.html for
    // command line options. NB: Built-in `-help` is incomplete.

    // TODO:
    // * copyright/license ( -BlicenseFile=LICENSE )
    // * environment variables?
    // * category ?

    val pairs = Map(
      "-name" -> name,
      "-srcdir" -> sourceDir.getAbsolutePath,
      "-native" -> packageType,
      "-outdir" -> outputDir.getAbsolutePath,
      "-outfile" -> basename,
      "-description" -> description,
      "-vendor" -> maintainer
    ) ++ mainClassArg

    val singles = Seq(
      s"-BappVersion=$version",
      s"-BmainJar=lib/$mainJar"
    ) ++ iconArg ++ jvmOptsArgs

    // Merge singles into argument pair map. (Need a cleaner abstraction)
    pairs ++ singles.map((_, "")).toMap
  }

  /** Generates a configure Process instance, ready to run. */
  private[jdkpackager] def makeProcess(
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

    // To help debug arguments, create a bash script doing the same.
    val script = file(argMap("-outdir")) / "jdkpackager.sh"
    IO.write(script, s"#!/bin/bash\n$argString\n")
    chmod(script, "766")

    Process(args)
  }

}
