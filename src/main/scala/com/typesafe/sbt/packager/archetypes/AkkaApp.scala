package com.typesafe.sbt
package packager
package archetypes

import sbt._
import sbt.Keys.{ target, sourceDirectory }

import packager.Keys.{ executableScriptName }
import SbtNativePackager.Universal

/**
 * == Deprecated ==
 *
 * Use `JavaAppPackaging` instead. Akka Microkernel will be removed soon.
 *
 * Provides a new default script for akka-micro-kernel applications.
 * This plugin requires the [[com.typesafe.sbt.packager.archetypes.JavaAppPackaging]],
 * which will be automatically enabled.
 *
 * @see [[http://doc.akka.io/docs/akka/snapshot/scala/microkernel.html]]
 * @see [[https://github.com/sbt/sbt-native-packager/pull/363]]
 *
 * @example Enable this plugin in your `build.sbt` with
 *
 * {{{
 *  enablePlugins(AkkaAppPackaging)
 * }}}
 *
 *
 */
@deprecated("Use JavaAppPackaging instead", "1.0.0")
object AkkaAppPackaging extends AutoPlugin with JavaAppStartScript {

  /**
   * Name of the bash template if user wants to provide custom one
   */
  val bashTemplate = "akka-bash-template"

  /**
   * Name of the bat template if user wants to provide custom one
   */
  val batTemplate = "akka-bat-template"

  override def requires = JavaAppPackaging

  override def projectSettings = settings

  import JavaAppPackaging.autoImport._

  private def settings: Seq[Setting[_]] = Seq(
    makeBashScript <<= (bashScriptDefines, target in Universal, executableScriptName, sourceDirectory) map makeUniversalBinScript(bashTemplate),
    makeBatScript <<= (batScriptReplacements, target in Universal, executableScriptName, sourceDirectory) map makeUniversalBatScript(batTemplate)
  )

}
