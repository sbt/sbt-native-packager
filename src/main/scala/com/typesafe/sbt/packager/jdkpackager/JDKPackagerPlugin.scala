package com.typesafe.sbt.packager.jdkpackager

import com.typesafe.sbt.SbtNativePackager
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.SettingsHelper
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.archetypes.jar.ClasspathJarPlugin
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
  override def requires = JavaAppPackaging // && ClasspathJarPlugin
  override lazy val projectSettings = javaPackagerSettings

  private val dirname = JDKPackager.name.toLowerCase

  def javaPackagerSettings: Seq[Setting[_]] = Seq(
    jdkPackagerTool := JDKPackagerHelper.locateJDKPackagerTool(),
    jdkAppIcon := None,
    jdkPackageType := "image",
    jdkPackagerBasename <<= packageName apply (_ + "-pkg"),
    mappings in JDKPackager <<= (mappings in Universal) map (_.filter(!_._2.startsWith("bin/")))
  ) ++ inConfig(JDKPackager)(
    Seq(
      sourceDirectory <<= sourceDirectory apply (_ / dirname),
      target <<= target apply (_ / dirname),
      mainClass <<= mainClass in Runtime,
      name <<= name,
      packageName <<= packageName,
      maintainer <<= maintainer,
      packageSummary <<= packageSummary,
      packageDescription <<= packageDescription,
      packagerArgMap <<= (
        name,
        version,
        packageDescription,
        jdkPackageType,
        mainClass,
        scriptClasspath,
        jdkPackagerBasename,
        jdkAppIcon,
        target,
        stage in Universal) map JDKPackagerHelper.mkArgMap
    )  ++ makePackageBuilder
  )

  private def checkTool(maybePath: Option[File]) = maybePath.getOrElse(
    sys.error("Please set key `jdkPackagerTool` to `javapackager` path")
  )

  private def makePackageBuilder = Seq(
    packageBin <<= (jdkPackagerTool, jdkPackageType, packagerArgMap, target, streams) map { (pkgTool, pkg, args, target, s) ⇒

      val tool = checkTool(pkgTool)
      val proc = JDKPackagerHelper.mkProcess(tool, "-deploy", args, s.log)

      (proc ! s.log) match {
        case 0 ⇒ ()
        case x ⇒ sys.error(s"Error running '$tool', exit status: $x")
      }

      // Oooof. Need to do better than this to determine what was generated.
      val root = file(args("-outdir"))
      val globs = Seq("*.dmg", "*.pkg", "*.app", "*.msi", "*.exe", "*.deb", "*.rpm")
      val finder = globs.foldLeft(PathFinder.empty)(_ +++ root ** _)
      val result = finder.getPaths.headOption
      result.foreach(f ⇒ s.log.info("Wrote " + f))
      // Not sure what to do when we can't find the result
      result.map(file).getOrElse(root)
    }
  )
}

object JDKPackagerDeployPlugin extends AutoPlugin {
  import JDKPackagerPlugin.autoImport._
  override def requires = JDKPackagerPlugin

  override def projectSettings =
    SettingsHelper.makeDeploymentSettings(JDKPackager, packageBin in JDKPackager, "jdkPackager")
}
