enablePlugins(JavaAppPackaging, AshScriptPlugin)

name := "command-line-app"

version := "0.1.0-SNAPSHOT"

TaskKey[Unit]("runCheck") := {
  val configArg = "-Dconfig.resource=/config.conf"
  val cwd = (stagingDirectory in Universal).value
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath, configArg)

  val output = (sys.process.Process(cmd, cwd).!!).replaceAll("\n", "")
  assert(output.contains(configArg), s"Application did not receive command line configuration resource $configArg")
}
