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
        packageDescription in JDKPackager <<= packageDescription
    ) ++ mapGenericFilesToPackager ++ inConfig(JDKPackager)(Seq(
        sourceDirectory <<= sourceDirectory apply (_ / dirname),
        target <<= target apply (_ / dirname),
        packageBin <<= (mappings, name, target, streams) map packagerDeploy
    ))

    /**
     * Apply mappings from Universal
     */
    def mapGenericFilesToPackager: Seq[Setting[_]] = Seq(
        mappings in JDKPackager <<= mappings in Universal
    )

    def packagerDeploy(mappings: Seq[(File, String)], name: String, target: File, streams: TaskStreams) = {
//        val appJar = (packageBin in JavaPackagerCreateJar).value
//        val tool = javaPackagerTool.value.getOrElse(
//            sys.error("Please set key `javaPackagerTool` to `javapackager` path")
//        )
//        val log = streams.value.log
//        val argMap = (packagerArgMap in JavaPackagerDeploy).value
//        val proc = JDKPackagerHelper.mkCommand(tool, "-deploy", argMap, log)
//        (proc ! log) match {
//            case 0 ⇒ ()
//            case x ⇒ sys.error(s"Error running '$tool', exit status: $x")
//        }
//        // fileMappings.foreach(println)
//        file(argMap("-outdir")) / (argMap("-outfile"))
        file(name + ".jar")
    }

}


object JDKPackagerDeployPlugin extends AutoPlugin {

    import JDKPackagerPlugin.autoImport._

    override def requires = JDKPackagerPlugin

    override def projectSettings =
        SettingsHelper.makeDeploymentSettings(JDKPackager, packageBin in JDKPackager, "jdkPackager")
}
