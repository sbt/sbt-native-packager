import sbt._

object PluginBuild extends Build {
  lazy val root = Project("plugins", file(".")) dependsOn(packager)

  lazy val packager = file("..").getAbsoluteFile.toURI
}
