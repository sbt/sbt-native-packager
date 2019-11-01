enablePlugins(AshStartScriptPlugin)

name := "simple-app"

version := "0.1.0"

TaskKey[Unit]("runCheck") := {
  val cwd = (stagingDirectory in Universal).value / "bin"
  val cmd = cwd / packageName.value + ".sh"
  val output = sys.process.Process(cmd).!!
  assert(output.contains("SUCCESS!"), "Output didn't contain success: " + output)
}
