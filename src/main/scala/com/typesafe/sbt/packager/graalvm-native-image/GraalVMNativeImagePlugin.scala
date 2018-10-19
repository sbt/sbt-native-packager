package com.typesafe.sbt.packager.graalvmnativeimage

import sbt._
import sbt.Keys._
import java.nio.charset.Charset

import com.typesafe.sbt.packager.SettingsHelper
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.linux._
import com.typesafe.sbt.packager.Compat._
import com.typesafe.sbt.packager.validation._

/**
  * Plugin to compile ahead-of-time native executables.
  *
  * @example Enable the plugin in the `build.sbt`
  * {{{
  *    enablePlugins(GraalVMNativeImagePlugin)
  * }}}
  */
object GraalVMNativeImagePlugin extends AutoPlugin {

  object autoImport extends GraalVMNativeImageKeys {
    val GraalVMNativeImage: Configuration = config("graalvm-native-image")
  }

  private val GraalVMNativeImageCommand = "native-image"

  import autoImport._

  override def projectConfigurations: Seq[Configuration] = Seq(GraalVMNativeImage)

  override lazy val projectSettings = Seq(
    target in GraalVMNativeImage := target.value / "graalvm-native-image",
    graalVMNativeImageOptions := Seq.empty,
    packageBin in GraalVMNativeImage := {
      val targetDirectory = (target in GraalVMNativeImage).value
      targetDirectory.mkdirs()
      val binaryName = name.value
      val command = {
        val nativeImageArguments = {
          val className = (mainClass in Compile).value.getOrElse(sys.error("Could not find a main class."))
          val classpathJars = Seq((packageBin in Compile).value) ++ (dependencyClasspath in Compile).value.map(_.data)
          val classpath = classpathJars.mkString(":")
          val extraOptions = graalVMNativeImageOptions.value
          Seq("--class-path", classpath, s"-H:Name=$binaryName") ++ extraOptions ++ Seq(className)
        }
        Seq(GraalVMNativeImageCommand) ++ nativeImageArguments
      }
      sys.process.Process(command, targetDirectory) ! streams.value.log match {
        case 0 => targetDirectory / binaryName
        case x => sys.error(s"Failed to run $GraalVMNativeImageCommand, exit status: " + x)
      }
    }
  )
}
