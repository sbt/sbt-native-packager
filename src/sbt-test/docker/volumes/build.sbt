enablePlugins(JavaAppPackaging, DockerPlugin)

name := "simple-test"

version := "0.1.0"

dockerExposedVolumes := Seq("/opt/docker/logs", "/opt/docker/config")
