enablePlugins(JavaAppPackaging)

name := "simple-test"

executableScriptName := "simple-exec"

version := "0.1.0"

TaskKey[Unit]("unzip") := {
  val args = Seq((packageBin in Universal).value.getAbsolutePath)
  Process("unzip", args) ! streams.value.log
}
