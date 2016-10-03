enablePlugins(JavaAppPackaging)

name := "simple-test"

executableScriptName := "simple-exec"

version := "0.1.0"

TaskKey[Unit]("unzip") <<= (packageBin in Universal, streams) map { (zipFile, streams) =>
  val args = Seq(zipFile.getAbsolutePath)
  Process("unzip", args) ! streams.log
}
