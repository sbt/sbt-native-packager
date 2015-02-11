package com.typesafe.sbt.packager.jdkpackager

import sbt._

/**
 * Support functions
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
            case Seq(1, 7)           ⇒ "javafxpackager"
            case _                   ⇒ sys.error("Need at least JDK version 1.7 to run JDKPackager plugin")
        }
        jdkHome
            .map(f ⇒ if (f.getName == "jre") f / ".." else f)
            .map(f ⇒ f / "bin" / toolname)
            .filter(_.exists())
    }

    def mkCommand(tool: File, mode: String, argMap: Map[String, String], log: Logger) = {
        val argPairs = argMap.map(p ⇒ Seq(p._1, p._2)).flatten
        val args = Seq(tool.getAbsolutePath, mode, "-v") ++ argPairs
        val argString = args.take(3).mkString(" ") + " " + argMap.map(p ⇒ s"${p._1}  '${p._2}'").mkString(" ")
        log.debug(s"Package command: $argString")
        Process(args)
    }
}
