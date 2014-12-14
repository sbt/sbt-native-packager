enablePlugins(JavaAppPackaging)

name := "test-custom-main"

version := "0.1.0"

mainClass in Compile := Some("Main")

TaskKey[Unit]("check-app-main") <<= (packageBin in Universal, streams) map { (zipFile, streams) =>
  val process = sbt.Process("target/universal/stage/bin/test-custom-main.bat")
  val out = (process!!)
  if (out.trim != "App Main Method") error("unexpected output: " + out)
  ()
}

TaskKey[Unit]("check-custom-main") <<= (packageBin in Universal, streams) map { (zipFile, streams) =>
  val process = sbt.Process("target/universal/stage/bin/test-custom-main.bat", Seq("-main", "CustomMain"))
  val out = (process!!)
  if (out.trim != "Custom Main Method") error("unexpected output: " + out)
  ()
}

