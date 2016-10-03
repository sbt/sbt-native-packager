enablePlugins(JavaAppPackaging)

name := "absolute-path-in-bat"

version := "0.1.0"

scriptClasspath in batScriptReplacements ++= Seq("x:\\dummy\\absolute\\path",
                                                 "relative\\path")

TaskKey[Unit]("run-check") := {
  val dir = (stagingDirectory in Universal).value

  val bat = IO.read(dir / "bin" / "absolute-path-in-bat.bat")
  assert(bat contains ";x:\\dummy\\absolute\\path")
  assert(bat contains "%APP_LIB_DIR%\\relative\\path")
}
