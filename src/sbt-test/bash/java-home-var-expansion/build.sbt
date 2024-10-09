import com.typesafe.sbt.packager.Compat._

enablePlugins(JavaAppPackaging)

name := "java-home-override"

version := "0.1.0"

javaOptions in Universal ++= Seq("-java-home ${app_home}/../jre")

TaskKey[Unit]("runCheck") := {
  val cwd = (Universal / stagingDirectory).value
  // Don't check for java but it will fail since the jre is not in place
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath, "-v", "-no-version-check")
  val output = sys.process.Process(cmd, cwd).lines_!
  val outStr = output.mkString("\n")
  // Check that ${app_home} has been substitued
  assert(outStr.contains("stage/bin/../jre/bin/java"), "Output didn't contain success: " + output)
}
