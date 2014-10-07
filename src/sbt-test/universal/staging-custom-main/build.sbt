enablePlugins(JavaAppPackaging)

name := "stage-custom-main"

version := "0.1.0"

mainClass in Compile := Some("Main")

TaskKey[Unit]("unzip") <<= (packageBin in Universal, streams) map { (zipFile, streams) =>
    val args = Seq(zipFile.getAbsolutePath)
    Process("unzip", args) !  streams.log
}

TaskKey[Unit]("check") <<= (packageBin in Universal, streams) map { (zipFile, streams) =>
  val process = sbt.Process("stage-custom-main-0.1.0/bin/stage-custom-main", Seq("-main", "CustomMain"))
  val out = (process!!)
  if (out.trim != "A custom main method") error("unexpected output: " + out)
  ()
}
