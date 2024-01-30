libraryDependencies <+= Def.setting[ModuleID] {
  Defaults
    .sbtPluginExtra(
      "com.typesafe.play" % "sbt-plugin" % "2.3.8",
      (sbtBinaryVersion in update).value,
      (scalaBinaryVersion in update).value
    )
    .exclude("com.github.sbt", "sbt-native-packager")
}

lazy val root = Project("plugins", file(".")) dependsOn (packager)

lazy val packager = file("../../").getAbsoluteFile.toURI
