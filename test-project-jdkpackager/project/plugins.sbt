lazy val root = Project("plugins", file(".")) dependsOn (packager)

lazy val packager = file("..").getAbsoluteFile.toURI
