import com.typesafe.sbt.packager.Keys.stagingDirectory

enablePlugins(JavaAppPackaging, AshScriptPlugin)

name := "script-debug"

version := "0.1.0"

TaskKey[Unit]("runCheck") := {
  val cwd = (Universal / stagingDirectory).value
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath, "-jvm-debug", "0")
  val output = (sys.process.Process(cmd, cwd).!!).replaceAll("\n", "")

  assert(output.contains("Listening for transport dt_socket at address:"),
    "Application did not start in debug mode: \n" + output)
  assert(output.contains("SUCCESS!"), "Application did not run successfully: \n" + output)
}
