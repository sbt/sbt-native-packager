enablePlugins(DockerPlugin)

name := "simple-test"

version := "0.1.0"

dockerExposedPorts := Seq(9000, 9001)
dockerExposedUdpPorts := Seq(10000, 10001)
