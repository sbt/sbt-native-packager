package com.typesafe.sbt.packager.jdkpackager

import com.typesafe.sbt.SbtNativePackager
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.SettingsHelper
import com.typesafe.sbt.packager.universal.UniversalPlugin
import sbt.Keys._
import sbt._
import SbtNativePackager.Universal
/**
 * Package format via Oracle's packaging tool bundled with JDK 7 & 8.
 */
object JDKPackagerPlugin extends AutoPlugin {

  object autoImport extends JDKPackagerKeys {
    val JDKPackager = config("jdkPackager") extend Universal
  }
  import autoImport._
  override def requires = UniversalPlugin
  override lazy val projectSettings = javaPackagerSettings

  private val dirname = "jdkpackager"

  def javaPackagerSettings: Seq[Setting[_]] = Seq(
    jdkPackagerTool := JDKPackagerHelper.locateJDKPackagerTool(),
    name in JDKPackager <<= name,
    packageName in JDKPackager <<= packageName,
    maintainer in JDKPackager <<= maintainer,
    packageSummary in JDKPackager <<= packageSummary,
    packageDescription in JDKPackager <<= packageDescription,
    mappings in JDKPackager <<= mappings in Universal
  ) ++ inConfig(JDKPackager)(
    Seq(
      jdkPackagerOutputBasename <<= packageName apply (_ + "-pkg"),
      sourceDirectory <<= sourceDirectory apply (_ / dirname),
      target <<= target apply (_ / dirname),
      mainClass <<= mainClass in Runtime
    ) ++ makeArgumentMap ++ makePackageBuilder
  )

  private def checkTool(maybePath: Option[File]) = maybePath.getOrElse(
    sys.error("Please set key `jdkPackagerTool` to `javapackager` path")
  )

  private def makeArgumentMap = Seq(
    packagerArgMap <<= (name, packageDescription, jdkPackagerOutputBasename, target, classDirectory in Compile, mainClass, stage in Universal) map { (name, desc, basename, outDir, srcDir, mainClass, stage) ⇒
      Map(
        "-name" -> name,
        "-appclass" -> (mainClass getOrElse sys.error("Main application class required.")),
        "-srcdir" -> stage.getAbsolutePath,
        "-native" -> "all",
        "-outdir" -> outDir.getAbsolutePath,
        "-outfile" -> basename
      )
    }
  )

  private def makePackageBuilder = Seq(
    packageBin <<= (jdkPackagerTool, packagerArgMap, streams) map { (pkgTool, args, s) ⇒
      val tool = checkTool(pkgTool)
      val proc = JDKPackagerHelper.mkProcess(tool, "-deploy", args, s.log)
      (proc ! s.log) match {
        case 0 ⇒ ()
        case x ⇒ sys.error(s"Error running '$tool', exit status: $x")
      }
      val output = file(args("-outdir")) / (args("-outfile") + ".jar")
      s.log.info("Wrote " + output)
      output
    }
  )
}

object JDKPackagerDeployPlugin extends AutoPlugin {
  import JDKPackagerPlugin.autoImport._
  override def requires = JDKPackagerPlugin

  override def projectSettings =
    SettingsHelper.makeDeploymentSettings(JDKPackager, packageBin in JDKPackager, "jdkPackager")
}
