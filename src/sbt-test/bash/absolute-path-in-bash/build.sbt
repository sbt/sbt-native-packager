enablePlugins(JavaAppPackaging)

name := "absolute-path-in-bash"

version := "0.1.0"

scriptClasspath in bashScriptDefines ++= Seq("/dummy/absolute/path", "relative/path")

TaskKey[Unit]("run-check") := {
  val dir = (stagingDirectory in Universal).value

  val bash = IO.read(dir / "bin" / "absolute-path-in-bash")
  assert(bash contains ":/dummy/absolute/path")
  assert(bash contains ":$lib_dir/relative/path")
}
