enablePlugins(JavaAppPackaging)

name := "docker-test"

packageName in Docker := "docker-package"

executableScriptName := "docker-exec"

version := "0.1.0"

maintainer := "Gary Coady <gary@lyranthe.org>"

TaskKey[Unit]("check-dockerfile") := {
  val dockerfile = IO.read(target.value / "docker" / "Dockerfile")
  assert(
    dockerfile.contains("ENTRYPOINT [\"bin/docker-exec\"]\n"),
    "dockerfile doesn't contain ENTRYPOINT [\"docker-exec\"]\n" + dockerfile
  )
  streams.value.log.success("Successfully tested control script")
  ()
}
