import com.typesafe.sbt.packager.Compat._

enablePlugins(JavaAppPackaging)

name := "simple-app"

version := "0.1.0"

batScriptExtraDefines += """call :add_java "-Dconfig.file=%APP_HOME%\conf\production.conf""""

TaskKey[Unit]("runCheck") := {
  val cwd = (stagingDirectory in Universal).value
  val cmd = Seq((cwd / "bin" / s"${packageName.value}.bat").getAbsolutePath)
  val configFile = (sys.process.Process(cmd, cwd).!!).replaceAll("\r\n", "")
  assert(configFile.contains("""stage\bin\\\..\conf\production.conf"""), "Output didn't contain config file path: " + configFile)
}
