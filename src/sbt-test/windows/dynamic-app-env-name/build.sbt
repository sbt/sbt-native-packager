import com.typesafe.sbt.packager.Compat._

enablePlugins(JavaAppPackaging)

name := "example-cli"

version := "0.1.0"

/*
 * Previous sbt-native-packager versions used a dynamic environment variable name %<APP_NAME>_HOME%
 * Now it is always called %APP_HOME% and the old name can be used for backwards compatibility.
 */

batScriptExtraDefines += """set _JAVA_OPTS=%_JAVA_OPTS% -Dconfig.file=%EXAMPLE_CLI_HOME%\\conf\\app.config"""

TaskKey[Unit]("runCheck") := {
  val cwd = (stagingDirectory in Universal).value
  val cmd = Seq((cwd / "bin" / s"${packageName.value}.bat").getAbsolutePath)
  val configFile = (sys.process.Process(cmd, cwd).!!).replaceAll("\r\n", "")
  assert(configFile.contains("""stage\bin\\\..\\conf\\app.config"""), "Output didn't contain config file path: " + configFile)
}
