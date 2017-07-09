import com.typesafe.sbt.packager.Compat._

enablePlugins(JavaAppPackaging)

name := "simple-test"

executableScriptName := "simple-exec"

version := "0.1.0"

TaskKey[Unit]("unzip") := {
  val args = Seq((packageBin in Universal).value.getAbsolutePath)
  sys.process.Process("unzip", args) ! streams.value.log
}
