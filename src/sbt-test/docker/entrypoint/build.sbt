enablePlugins(DockerPlugin)

name := "simple-test"

version := "0.1.0"

dockerEntrypoint := Seq("/bin/sh", "-c", "env")
