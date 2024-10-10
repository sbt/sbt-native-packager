enablePlugins(JavaAppPackaging)

name := "absolute-path-in-bat"

version := "0.1.0"

(batScriptReplacements / scriptClasspath) ++= Seq("x:\\dummy\\absolute\\path", "relative\\path")

TaskKey[Unit]("runCheck") := {
  val dir = (Universal / stagingDirectory).value

  val bat = IO.read(dir / "bin" / "absolute-path-in-bat.bat")
  assert(bat contains ";x:\\dummy\\absolute\\path")
  assert(bat contains "%APP_LIB_DIR%\\relative\\path")
}
