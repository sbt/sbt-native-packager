enablePlugins(JavaAppPackaging, AshScriptPlugin)

name := "simple-app"

version := "0.1.0"

bashScriptExtraDefines ++= Seq("""addApp "info"""", """addApp "help"""")

TaskKey[Unit]("scriptCheck") := {
  val startScript = (Universal / stagingDirectory).value / "bin" / executableScriptName.value
  val options = IO.read(startScript)
  assert(options contains """addApp "info"""", "Script doesn't contain app setting:\n" + options)
  assert(options contains """addApp "help"""", "Script doesn't contain app setting:\n" + options)
}

TaskKey[Unit]("runCheck") := {
  val cwd = (Universal / stagingDirectory).value
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath)
  val output = (sys.process.Process(cmd, cwd).!!).replaceAll("\n", "")

  assert(output.contains("info"), s"Application did not receive residual arg 'info'")
  assert(output.contains("help"), s"Application did not receive residual arg 'help'")
}
