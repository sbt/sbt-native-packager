lazy val root = Project("plugins", file(".")) dependsOn(packager)

lazy val packager = file("..").getAbsoluteFile.toURI

libraryDependencies += "org.vafer" % "jdeb" % "1.3" artifacts (Artifact("jdeb", "jar", "jar"))