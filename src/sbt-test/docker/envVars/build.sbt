enablePlugins(DockerPlugin)

name := "simple-test"

version := "0.1.0"

dockerEnvVars := Map("FOO" -> "bar", "FOO_BAR" -> "foo bar", "NUMBER" -> "123")


TaskKey[Unit]("checkDockerfile") := {
  val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")

  assert(dockerfile contains """ENV FOO="bar"""", s"does not contain foo=bar\n$dockerfile")
  assert(dockerfile contains """ENV FOO_BAR="foo bar"""", s"does not contain foo=bar\n$dockerfile")
  assert(dockerfile contains """ENV NUMBER="123"""", s"does not contain foo=bar\n$dockerfile")

  streams.value.log.success("Successfully tested Dockerfile")
  ()
}
