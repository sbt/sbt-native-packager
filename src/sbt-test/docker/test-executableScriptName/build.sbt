enablePlugins(JavaAppPackaging)

name := "docker-test"

packageName in Docker := "docker-package"

executableScriptName := "docker-exec"

version := "0.1.0"

maintainer := "Gary Coady <gary@lyranthe.org>"

TaskKey[Unit]("checkDockerfile") := {
  val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")
  assert(
    dockerfile.contains("ENTRYPOINT [\"/opt/docker/bin/docker-exec\"]\n"),
    "dockerfile doesn't contain ENTRYPOINT [\"/opt/docker/bin/docker-test\"]\n" + dockerfile
  )
  streams.value.log.success("Successfully tested control script")
  ()
}
