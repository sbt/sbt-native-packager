enablePlugins(DockerPlugin)

name := "simple-test"

version := "0.1.0"

dockerExposedVolumes in Docker := Seq("/opt/docker/logs", "/opt/docker/config")
