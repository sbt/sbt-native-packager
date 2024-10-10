import com.typesafe.sbt.packager.Compat._

enablePlugins(JavaAppPackaging)

name := "test-custom-main"

version := "0.1.0"

(Compile / mainClass) := Some("Main")

TaskKey[Unit]("checkAppMain") := {
  val zipFile = (Universal / packageBin).value
  val process =
    sys.process.Process("target/universal/stage/bin/test-custom-main.bat")
  val out = (process !!)
  if (out.trim != "App Main Method") sys.error("unexpected output: " + out)
  ()
}

TaskKey[Unit]("checkCustomMain") := {
  val zipFile = (Universal / packageBin).value
  val process =
    sys.process.Process("target/universal/stage/bin/test-custom-main.bat", Seq("-main", "CustomMain"))
  val out = (process !!)
  if (out.trim != "Custom Main Method") sys.error("unexpected output: " + out)
  ()
}
