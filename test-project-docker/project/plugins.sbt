lazy val packager =  ProjectRef(file("../.."), "sbt-native-packager")
dependsOn(packager)

// needs to be added for the docker spotify client
libraryDependencies += "com.spotify" % "docker-client" % "3.5.13"
