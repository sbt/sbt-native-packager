enablePlugins(AshStartScriptPlugin)

name := "multiple-apps"

version := "0.1.0-SNAPSHOT"

TaskKey[Unit]("runCheckFoo") := {
  val arguments = "-Xmx50m -Xms20m foo".split(" ")
  val cwd = (stagingDirectory in Universal).value / "bin"
  val cmd = cwd + "/foo.sh"
  val output: String = sys.process.Process(cmd, arguments).!!
  assert(output.contains("Foo works"))
  assert(arguments.forall(output.contains), s"Script did not exist or did not receive command line arguments $arguments")
}

TaskKey[Unit]("runCheckBar") := {
  val arguments = "-Xmx80m -Xms20m bar".split(" ")
  val cwd = (stagingDirectory in Universal).value / "bin"
  val cmd = cwd + "/bar.sh"
  val output: String = sys.process.Process(cmd, arguments).!!
  assert(output.contains("Bar works"))
  assert(arguments.forall(output.contains), s"Script did not exist or did not receive command line arguments $arguments")
}
