addSbtPlugin("com.github.sbt" % "sbt-native-packager" % sys.props("project.version"))

// needs to be added for the docker spotify client
libraryDependencies += "com.spotify" % "docker-client" % "3.5.13"
