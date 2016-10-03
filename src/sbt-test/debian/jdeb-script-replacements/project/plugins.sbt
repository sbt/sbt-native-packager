addSbtPlugin(
  "com.typesafe.sbt" % "sbt-native-packager" % sys.props("project.version"))

libraryDependencies += "org.vafer" % "jdeb" % "1.3" artifacts (Artifact("jdeb",
                                                                        "jar",
                                                                        "jar"))
