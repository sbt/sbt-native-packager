import com.typesafe.sbt.packager.Compat._

enablePlugins(JavaAppPackaging)

name := "stage-custom-main"

version := "0.1.0"

(Compile / mainClass) := Some("Main")

TaskKey[Unit]("unzip") := {
  val args = Seq(((Universal / packageBin)).value.getAbsolutePath)
  sys.process.Process("unzip", args) ! streams.value.log
}

TaskKey[Unit]("check") := {
  val zipFile = ((Universal / packageBin)).value
  val process = sys.process.Process("stage-custom-main-0.1.0/bin/stage-custom-main", Seq("-main", "CustomMain"))
  val out = (process !!)
  if (out.trim != "A custom main method") sys.error("unexpected output: " + out)
  ()
}
