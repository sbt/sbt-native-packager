enablePlugins(JavaAppPackaging)

name := "docker-build-options-test"

version := "0.1.0"

dockerBuildOptions := dockerBuildOptions.value ++ Seq(
  "-t",
  "docker-build-options-test:0.1.0-random-tag")
