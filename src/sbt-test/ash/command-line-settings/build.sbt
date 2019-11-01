enablePlugins(AshStartScriptPlugin)

name := "command-line-app"

version := "0.1.0-SNAPSHOT"

TaskKey[Unit]("runCheck") := {
  val log = streams.value.log
  val arguments = "-Xms50m -Xmn20m foo bar".split(" ")
  val cwd = (stagingDirectory in Universal).value / "bin"
  val cmd = cwd / packageName.value + ".sh"
  val output: String = sys.process.Process(cmd, arguments).!!
  log.info(s"cmd = $cmd")
  log.info(s"cwd = $cwd")
  log.info(s"output = $output")
  assert(arguments.forall(output.contains), s"Application did not receive command line arguments $arguments")
}
