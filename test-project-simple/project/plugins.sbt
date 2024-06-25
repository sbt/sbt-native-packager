lazy val packager = ProjectRef(file("../.."), "sbt-native-packager")
dependsOn(packager)

libraryDependencies += "org.vafer" % "jdeb" % "1.3" artifacts (Artifact("jdeb", "jar", "jar"))
