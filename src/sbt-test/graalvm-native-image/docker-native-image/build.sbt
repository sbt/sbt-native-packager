enablePlugins(GraalVMNativeImagePlugin)

name := "docker-test"
version := "0.1.0"
containerBuildImage := Def
  .taskDyn(GraalVMNativeImagePlugin.generateContainerBuildImage("ghcr.io/graalvm/graalvm-community:25i5-25.0.5.0.3"))
  .value
