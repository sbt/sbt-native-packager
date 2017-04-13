enablePlugins(DockerPlugin)

name := "simple-test"

version := "0.1.0"

dockerLabels := Map("foo" -> "bar", "Hello" -> "World")
