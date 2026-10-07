libraryDependencies +=
  Defaults
    .sbtPluginExtra(
      "org.playframework" % "sbt-plugin" % "3.0.9",
      (update / sbtBinaryVersion).value,
      (update / scalaBinaryVersion).value
    )
    .exclude("com.github.sbt", "sbt-native-packager")

lazy val root = Project("plugins", file(".")) dependsOn (packager)

lazy val packager = ProjectRef(file("../../.."), "sbt-native-packager")
