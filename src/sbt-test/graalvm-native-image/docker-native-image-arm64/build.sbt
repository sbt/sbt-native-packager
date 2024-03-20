enablePlugins(GraalVMNativeImagePlugin)

name := "docker-test"
version := "0.1.0"
graalVMNativeImageOptions := Seq("--no-fallback")
graalVMNativeImageGraalVersion := Some("native-image-community:17.0.8")
graalVMNativeImagePlatformArch := Some("arm64")
