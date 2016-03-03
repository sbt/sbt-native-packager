lazy val root = Project("plugins", file(".")).dependsOn(plugin)

lazy val plugin = file("../").getCanonicalFile.toURI

// needs to be added for the docker spotify client
libraryDependencies += "com.spotify" % "docker-client" % "3.5.13"