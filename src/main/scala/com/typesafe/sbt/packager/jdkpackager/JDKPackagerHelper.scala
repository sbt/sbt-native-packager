package com.typesafe.sbt.packager.jdkpackager

import com.typesafe.sbt.packager.chmod
import sbt._
import scala.util.Try
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
/**
 * Support/helper functions for interacting with the `javapackager` tool.
 * @author <a href="mailto:fitch@datamininglab.com">Simeon H.K. Fitch</a>
 * @since 2/11/15
 */
object JDKPackagerHelper {

  /** Attempts to compute the path to the `javapackager` tool. */
  def locateJDKPackagerTool(javaHome: Option[File]): Option[File] = {
    val toolname = sys.props("os.name").toLowerCase match {
      case os if os.contains("win") ⇒ "javapackager.exe"
      case _                        ⇒ "javapackager"
    }

    // This approach to getting JDK bits is borrowed from: http://stackoverflow.com/a/25163628/296509
    // Starting with an ordered list of possible java directory sources, create derivative and
    // then test for the tool. It's nasty looking because there's no canonical way of finding the
    // JDK from the JRE, and JDK_HOME isn't always defined.
    Seq(
      // Build-defined
      javaHome,
      // Environment override
      sys.env.get("JDK_HOME").map(file),
      sys.env.get("JAVA_HOME").map(file),
      // MacOS X
      Try("/usr/libexec/java_home".!!.trim).toOption.map(file),
      // From system properties
      sys.props.get("java.home").map(file)
    )
      // Unlift Option-s
      .flatten
      // For each base directory, add the parent variant to cover nested JREs on Unix.
      .flatMap {
        f ⇒ Seq(f, f.getAbsoluteFile)
      }
      // On Windows we're often running in the JRE and not the JDK. If JDK is installed,
      // it's likely to be in a parallel directory, with the "jre" prefix changed to "jdk"
      .flatMap { f ⇒
        if (f.getName.startsWith("jre")) {
          Seq(f, f.getParentFile / ("jdk" + f.getName.drop(3)))
        } else Seq(f)
      }
      // Now search for the tool
      .map { n =>
        n / "bin" / toolname
      }
      .find(_.exists)
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
    mainJar: File,
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

    val vendorArg = if (maintainer.nonEmpty)
      Map("-vendor" -> maintainer) else Map.empty

    val descriptionArg = if (description.nonEmpty)
      Map("-description" -> description) else Map.empty

    // Make a setting?
    val jvmOptsFile = (sourceDir ** JavaAppPackaging.appIniLocation).getPaths.headOption.map(file)

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
      "-outfile" -> basename
    ) ++ mainClassArg ++ vendorArg ++ descriptionArg

    val singles = Seq(
      s"-BappVersion=$version",
      s"-BmainJar=lib/${mainJar.getName}"
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
    Try(chmod(script, "766"))

    Process(args)
  }

}
