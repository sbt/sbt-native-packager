enablePlugins(GraalVMNativeImagePlugin)

name := "docker-test"
version := "0.1.0"
graalVMNativeImageOptions := Seq("--no-fallback")
graalVMNativeImageGraalVersion := Some("native-image:22.3.3")
