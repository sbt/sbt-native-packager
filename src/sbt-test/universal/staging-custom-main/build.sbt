enablePlugins(JavaAppPackaging)

name := "stage-custom-main"

version := "0.1.0"

mainClass in Compile := Some("Main")

TaskKey[Unit]("unzip") := {
  val args = Seq((packageBin in Universal).value.getAbsolutePath)
  Process("unzip", args) ! streams.value.log
}

TaskKey[Unit]("check") := {
  val zipFile = (packageBin in Universal).value
  val process = sbt.Process("stage-custom-main-0.1.0/bin/stage-custom-main", Seq("-main", "CustomMain"))
  val out = (process !!)
  if (out.trim != "A custom main method") sys.error("unexpected output: " + out)
  ()
}
