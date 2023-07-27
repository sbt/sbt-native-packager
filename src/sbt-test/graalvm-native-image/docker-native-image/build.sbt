enablePlugins(GraalVMNativeImagePlugin)

name := "docker-test"
version := "0.1.0"
graalVMNativeImageOptions := Seq("--no-fallback")
graalVMNativeImageGraalVersion := Some("graalvm-community:17.0.8")
