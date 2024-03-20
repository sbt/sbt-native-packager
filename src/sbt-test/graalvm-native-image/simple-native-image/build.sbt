enablePlugins(GraalVMNativeImagePlugin)

name := "simple-test"
version := "0.1.0"
graalVMNativeImageOptions := Seq("--no-fallback")
