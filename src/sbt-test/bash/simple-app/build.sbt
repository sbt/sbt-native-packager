import com.typesafe.sbt.packager.Compat._

scalaVersion := "2.12.20"

enablePlugins(JavaAppPackaging)

name := "simple-app"

version := "0.1.0"

TaskKey[Unit]("runCheck") := {
  val cwd = (Universal / stagingDirectory).value
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath)
  val output = sys.process.Process(cmd, cwd).!!
  assert(output contains "SUCCESS!", "Output didn't contain success: " + output)
}
