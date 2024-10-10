// Tests basic jlink plugin functionality.

import scala.sys.process.Process
import com.typesafe.sbt.packager.Compat._

enablePlugins(JlinkPlugin, ClasspathJarPlugin, BashStartScriptPlugin, BatStartScriptPlugin)

// Exclude Scala to avoid linking additional modules
autoScalaLibrary := false
(Compile / packageDoc / mappings) := Seq()

TaskKey[Unit]("runChecks") := {
  val log = streams.value.log

  def run(exe: String, args: Seq[String]): String = {
    log.info(s"Running '$exe ${args.mkString(" ")}'")
    Process(exe, args) !! log
  }

  val (extension, os) = sys.props("os.name").toLowerCase match {
    case os if os.contains("mac") ⇒ (".app", 'mac)
    case os if os.contains("win") ⇒ (".exe", 'windows)
    case _ ⇒ ("", 'linux)
  }

  val stageDir = (Universal / stagingDirectory).value
  val bundledJvmDir = (stageDir / "jre")
  val javaExe = (bundledJvmDir / "bin" / ("java" + extension)).getAbsolutePath

  // This is useful for debugging.
  val releaseInfo = IO.read(bundledJvmDir / "release")
  log.info(s"Produced image:\n$releaseInfo")

  // Run the application directly.
  val classpathJar = (stageDir / "lib" / packageJavaClasspathJar.value.getName).getAbsolutePath
  run(javaExe, Seq("-cp", classpathJar, "JlinkTestApp"))

  // Make sure the scripts use the correct JVM
  val startScripts = (os match {
    case 'windows => makeBatScripts.value.map(_._2)
    case _        => makeBashScripts.value.map(_._2)
  }).map(s => (stageDir / s).getAbsolutePath)

  startScripts.foreach { script =>
    run(script, Nil)
  }
}
