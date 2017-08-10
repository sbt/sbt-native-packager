enablePlugins(JavaAppPackaging, AshScriptPlugin)

name := "simple-app"

version := "0.1.0"

bashScriptExtraDefines ++= Seq("""addJava "-Xms64m"""", """addJava "-Xmx64m"""")

TaskKey[Unit]("scriptCheck") := {
  val startScript = (stagingDirectory in Universal).value / "bin" / executableScriptName.value
  val options = IO.read(startScript)
  assert(options contains """addJava "-Xms64m"""", "Script doesn't contain xmx setting:\n" + options)
  assert(options contains """addJava "-Xmx64m"""", "Script doesn't contain xms setting:\n" + options)
}

TaskKey[Unit]("runCheck") := {
  val cwd = (stagingDirectory in Universal).value
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath)
  val memory = (sys.process.Process(cmd, cwd).!!).replaceAll("\n", "")
  assert(memory.toLong <= 64, "Runtime memory is bigger then 64m < " + memory + "m")
}
