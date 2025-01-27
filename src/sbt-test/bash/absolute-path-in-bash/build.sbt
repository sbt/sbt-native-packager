enablePlugins(JavaAppPackaging)

name := "absolute-path-in-bash"

version := "0.1.0"

bashScriptDefines / scriptClasspath ++= Seq("/dummy/absolute/path", "relative/path")

TaskKey[Unit]("runCheck") := {
  val dir = (Universal / stagingDirectory).value

  val bash = IO.read(dir / "bin" / "absolute-path-in-bash")
  assert(bash contains ":/dummy/absolute/path")
  assert(bash contains ":$lib_dir/relative/path")
}
