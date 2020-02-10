enablePlugins(AshStartScriptPlugin)

name := "single-main-with-additional"

version := "0.1.0-SNAPSHOT"

mainClass in Compile := Some("MainApp")

TaskKey[Unit]("runCheckMainApp") := {
  val cwd = (stagingDirectory in Universal).value / "bin"
  val cmd = cwd / executableScriptName.value + ".sh"
  val output: String = sys.process.Process(cmd).!!
  assert(output.contains("MainApp works"))
}

TaskKey[Unit]("runCheckFoo") := {
  val cwd = (stagingDirectory in Universal).value / "bin"
  val cmd = cwd + "/foo.sh"
  val output: String = sys.process.Process(cmd).!!
  streams.value.log.warn(s"OUTPUT : $output")
  assert(output.contains("Foo works"))
}

TaskKey[Unit]("runCheckBar") := {
  val cwd = (stagingDirectory in Universal).value / "bin"
  val cmd = cwd + "/bar.sh"
  val output: String = sys.process.Process(cmd).!!
  assert(output.contains("Bar works"))
}
