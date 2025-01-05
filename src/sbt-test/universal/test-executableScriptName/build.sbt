import com.typesafe.sbt.packager.Compat._
import com.typesafe.sbt.packager.PluginCompat
import xsbti.FileConverter

enablePlugins(JavaAppPackaging)

name := "simple-test"

executableScriptName := "simple-exec"

version := "0.1.0"

TaskKey[Unit]("unzip") := {
  implicit val converter: FileConverter = fileConverter.value
  val args = Seq(PluginCompat.toFile((Universal / packageBin).value).getAbsolutePath)
  sys.process.Process("unzip", args) ! streams.value.log
}
