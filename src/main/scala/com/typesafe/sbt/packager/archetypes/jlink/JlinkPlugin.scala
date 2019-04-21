package com.typesafe.sbt.packager.archetypes
package jlink

import scala.sys.process.{Process, ProcessBuilder}
import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtNativePackager.{Debian, Universal}
import com.typesafe.sbt.packager.Keys.{bundledJvmLocation, packageName}
import com.typesafe.sbt.packager.Compat._
import com.typesafe.sbt.packager.archetypes.jlink._
import com.typesafe.sbt.packager.archetypes.scripts.BashStartScriptKeys
import com.typesafe.sbt.packager.universal.UniversalPlugin

/**
  * == Jlink Application ==
  *
  * This class contains the default settings for creating and deploying an
  * application as a runtime image using the standard `jlink` utility.
  *
  * == Configuration ==
  *
  * This plugin adds new settings to configure your packaged application.
  * The keys are defined in [[com.typesafe.sbt.packager.archetypes.jlink.JlinkKeys]]
  *
  * @example Enable this plugin in your `build.sbt` with
  *
  * {{{
  *  enablePlugins(JlinkPlugin)
  * }}}
  */
object JlinkPlugin extends AutoPlugin {

  object autoImport extends JlinkKeys

  import autoImport._

  override def requires = JavaAppPackaging

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    target in jlinkBuildImage := target.value / "jlink" / "output",
    jlinkBundledJvmLocation := "jre",
    bundledJvmLocation := Some(jlinkBundledJvmLocation.value),
    jlinkOptions := (jlinkOptions ?? Nil).value,
    jlinkOptions ++= {
      val log = streams.value.log
      val run = runJavaTool(javaHome.in(jlinkBuildImage).value, log) _

      val paths = fullClasspath.in(Compile).value.map(_.data.toString)
      val modules =
        (run("jdeps", "-R" +: "--print-module-deps" +: paths) !! log).trim
          .split(",")

      JlinkOptions(addModules = modules, output = Some(target.in(jlinkBuildImage).value))
    },
    jlinkBuildImage := {
      val log = streams.value.log
      val run = runJavaTool(javaHome.in(jlinkBuildImage).value, log) _
      val outDir = target.in(jlinkBuildImage).value

      IO.delete(outDir)

      run("jlink", jlinkOptions.value) !! log

      outDir
    },
    mappings in jlinkBuildImage := {
      val prefix = jlinkBundledJvmLocation.value
      // make sure the prefix has a terminating slash
      val prefix0 = if (prefix.isEmpty) prefix else (prefix + "/")

      findFiles(jlinkBuildImage.value).map {
        case (file, string) => (file, prefix0 + string)
      }
    },
    mappings in Universal ++= mappings.in(jlinkBuildImage).value
  )

  // TODO: deduplicate with UniversalPlugin and DebianPlugin
  /** Finds all files in a directory. */
  private def findFiles(dir: File): Seq[(File, String)] =
    ((PathFinder(dir) ** AllPassFilter) --- dir)
      .pair(file => IO.relativize(dir, file))

  private def runJavaTool(jvm: Option[File], log: Logger)(exeName: String, args: Seq[String]): ProcessBuilder = {
    val exe = jvm match {
      case None     => exeName
      case Some(jh) => (jh / "bin" / exeName).getAbsolutePath
    }

    log.info("Running: " + (exe +: args).mkString(" "))

    Process(exe, args)
  }

  private object JlinkOptions {
    def apply(addModules: Seq[String] = Nil, output: Option[File] = None): Seq[String] =
      option("--output", output) ++
        list("--add-modules", addModules)

    private def option[A](arg: String, value: Option[A]): Seq[String] =
      value.toSeq.flatMap(a => Seq(arg, a.toString))

    private def list(arg: String, values: Seq[String]): Seq[String] =
      if (values.nonEmpty) Seq(arg, values.mkString(",")) else Nil
  }
}
