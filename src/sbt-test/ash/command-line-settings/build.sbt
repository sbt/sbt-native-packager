enablePlugins(JavaAppPackaging, AshScriptPlugin)

scalaVersion := "2.12.20"

name := "command-line-app"

version := "0.1.0-SNAPSHOT"

TaskKey[Unit]("checkSystemProperty") := {
  val configArg = "config.resource=/config.conf"
  val cwd = (Universal / stagingDirectory).value
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath, s"-D$configArg")

  val output = (sys.process.Process(cmd, cwd).!!).replaceAll("\n", "")
  assert(output.contains(configArg), s"Application did not receive system property arg '$configArg'")
}

TaskKey[Unit]("checkResidual") := {
  val arg = "residualArg"
  val cwd = (Universal / stagingDirectory).value
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath, arg)

  val output = (sys.process.Process(cmd, cwd).!!).replaceAll("\n", "")
  assert(output.contains(arg), s"Application did not receive residual arg '$arg'")
}

TaskKey[Unit]("checkComplexResidual") := {
  val args = Seq(
    "-J-Dfoo=bar",
    "arg1",
    "--",
    "-J-Dfoo=bar",
    "arg 2",
    "--",
    "\"",
    "$foo",
    "'",
    "%s",
    "-y",
    "bla",
    "\\'",
    "\\\"",
    "''"
  )
  val cwd = (Universal / stagingDirectory).value
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath) ++ args
  val expected = """arg1|-J-Dfoo=bar|arg 2|--|"|$foo|'|%s|-y|bla|\'|\"|''"""

  val output = (sys.process.Process(cmd, cwd).!!).split("\n").last
  assert(output == expected, s"Application did not receive residual args '$expected' (got '$output')")
}
