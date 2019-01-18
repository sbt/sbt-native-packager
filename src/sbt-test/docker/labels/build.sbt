enablePlugins(JavaAppPackaging, DockerPlugin)

name := "simple-test"

version := "0.1.0"

dockerLabels := Map("foo" -> "bar", "fooBar" -> "foo bar", "number" -> "123")


TaskKey[Unit]("checkDockerfile") := {
  val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")

  assert(dockerfile contains """LABEL foo="bar"""", s"does not contain foo=bar\n$dockerfile")
  assert(dockerfile contains """LABEL fooBar="foo bar"""", s"does not contain foo=bar\n$dockerfile")
  assert(dockerfile contains """LABEL number="123"""", s"does not contain foo=bar\n$dockerfile")

  streams.value.log.success("Successfully tested Dockerfile")
  ()
}
