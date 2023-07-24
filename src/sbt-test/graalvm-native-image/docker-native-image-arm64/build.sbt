enablePlugins(GraalVMNativeImagePlugin)

name := "docker-test"
version := "0.1.0"
graalVMNativeImageOptions := Seq("--no-fallback")
graalVMNativeImageGraalVersion := Some("22.3.2")
graalVMNativeImagePlatformArch := Some("arm64")
