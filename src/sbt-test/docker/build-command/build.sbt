enablePlugins(JavaAppPackaging)

name := "docker-build-command-test"

version := "0.1.0"

import NativePackagerHelper._
(Docker / mappings) ++= directory("src/main/resources/docker-test")
dockerBuildCommand := Seq("docker", "build", "-t", "docker-build-command-test:0.1.0", "docker-test/")
