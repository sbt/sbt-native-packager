enablePlugins(JavaAppPackaging, DockerPlugin)

name := "simple-test"

version := "0.1.0"

dockerExposedPorts := Seq()
dockerExposedUdpPorts := Seq(10000, 10001)
