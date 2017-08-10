enablePlugins(JavaAppPackaging)

name := "absolute-path"

version := "0.1.0"

scriptClasspath in bashScriptDefines ++= Seq("/dummy/absolute/path", "relative/path")

scriptClasspath in batScriptReplacements ++= Seq("x:\\dummy\\absolute\\path", "relative\\path")

TaskKey[Unit]("check") := {
  val dir = (stagingDirectory in Universal).value

  val bash = IO.read(dir / "bin" / "absolute-path")
  assert(bash contains ":/dummy/absolute/path")
  assert(bash contains ":$lib_dir/relative/path")

  val bat = IO.read(dir / "bin" / "absolute-path.bat")
  assert(bat contains ";x:\\dummy\\absolute\\path")
  assert(bat contains "%APP_LIB_DIR%\\relative\\path")
}
