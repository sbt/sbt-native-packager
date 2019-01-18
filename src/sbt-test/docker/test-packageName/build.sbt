enablePlugins(JavaAppPackaging)

organization := "com.example"
name := "docker-test"

// packageName := "docker-package" // sets the executable script, too
packageName in Docker := "docker-package"

version := "0.1.0"

maintainer := "Gary Coady <gary@lyranthe.org>"

TaskKey[Unit]("checkDockerfile") := {
  val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")
  assert(
    dockerfile.contains("ENTRYPOINT [\"/opt/docker/bin/docker-test\"]\n"),
    "dockerfile doesn't contain ENTRYPOINT [\"/opt/docker/bin/docker-test\"]\n" + dockerfile
  )
  streams.value.log.success("Successfully tested control script")
  ()
}
