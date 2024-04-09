enablePlugins(JavaAppPackaging, AshScriptPlugin)

name := "command-line-app"

version := "0.1.0-SNAPSHOT"

TaskKey[Unit]("checkSystemProperty") := {
  val configArg = "config.resource=/config.conf"
  val cwd = (stagingDirectory in Universal).value
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath, s"-D$configArg")

  val output = (sys.process.Process(cmd, cwd).!!).replaceAll("\n", "")
  assert(output.contains(configArg), s"Application did not receive system property arg '$configArg'")
}

TaskKey[Unit]("checkResidual") := {
  val arg = "residualArg"
  val cwd = (stagingDirectory in Universal).value
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath, arg)

  val output = (sys.process.Process(cmd, cwd).!!).replaceAll("\n", "")
  assert(output.contains(arg), s"Application did not receive residual arg '$arg'")
}

TaskKey[Unit]("checkComplexResidual") := {
  val args = Seq("arg1", "arg 2", "\"", "$foo", "'", "%s")
  val cwd = (stagingDirectory in Universal).value
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath) ++ args

  val output = (sys.process.Process(cmd, cwd).!!).replaceAll("\n", "")
  assert(output.contains(args.mkString("|")), s"Application did not receive residual args '$args' ('$output')")
}
