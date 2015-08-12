enablePlugins(AshScriptPlugin)

name := "simple-app"

version := "0.1.0"

TaskKey[Unit]("run-check") := { 
  val cwd = (stagingDirectory in Universal).value
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath)
  val output = Process(cmd, cwd).!!
  assert(output contains "SUCCESS!", "Output didn't contain success: " + output)
}
