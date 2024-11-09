import xsbti.FileConverter
import com.typesafe.sbt.packager.PluginCompat
import NativePackagerHelper._

enablePlugins(JavaAppPackaging)

name := "docker-build-command-test"

version := "0.1.0"

Docker / mappings ++= {
  implicit val converter: FileConverter = fileConverter.value
  PluginCompat.toFileRefsMapping(directory("src/main/resources/docker-test"))
}
dockerBuildCommand := Seq("docker", "build", "-t", "docker-build-command-test:0.1.0", "docker-test/")
