enablePlugins(JavaAppPackaging)

name := "docker-test"

packageName := "docker-package"

version := "0.1.0"

maintainer := "Gary Coady <gary@lyranthe.org>"

TaskKey[Unit]("checkDockerfile") := {
  val dockerfile = IO.read(((Docker / stagingDirectory)).value / "Dockerfile")
  assert(
    dockerfile.contains("ENTRYPOINT [\"/opt/docker/bin/docker-test\"]\n"),
    "dockerfile doesn't contain ENTRYPOINT [\"/opt/docker/bin/docker-test\"]\n" + dockerfile
  )
  streams.value.log.success("Successfully tested control script")
  ()
}
