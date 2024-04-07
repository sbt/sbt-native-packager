import com.typesafe.sbt.packager.Compat._

enablePlugins(JavaAppPackaging)

name := "top-level-main"

version := "0.1.0"

scalaVersion := "3.3.3"

TaskKey[Unit]("runCheck") := {
  val cwd = (stagingDirectory in Universal).value
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath)
  val output = sys.process.Process(cmd, cwd).!!
  assert(output contains "SUCCESS!", "Output didn't contain success: " + output)
}
