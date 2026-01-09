val basename = "jdk-versions"

ThisBuild / Compile / compile / scalacOptions := Seq("-target:jvm-1.8")

lazy val `jdk8` = project
  .in(file("jdk8"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := basename + "-8",
    dockerBaseImage := "eclipse-temurin:8",
    dockerBuildOptions := dockerBuildOptions.value ++ Seq("-t", "jdk-versions:8")
  )

lazy val `jdk11` = project
  .in(file("jdk11"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := basename + "-11",
    dockerBaseImage := "eclipse-temurin:11",
    dockerBuildOptions := dockerBuildOptions.value ++ Seq("-t", "jdk-versions:11")
  )

lazy val `jdk17` = project
  .in(file("jdk17"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := basename + "-17",
    dockerBaseImage := "eclipse-temurin:17",
    dockerBuildOptions := dockerBuildOptions.value ++ Seq("-t", "jdk-versions:17")
  )

lazy val `jdk21` = project
  .in(file("jdk21"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := basename + "-21",
    dockerBaseImage := "eclipse-temurin:21",
    dockerBuildOptions := dockerBuildOptions.value ++ Seq("-t", "jdk-versions:21")
  )


lazy val `jdk25` = project
  .in(file("jdk25"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := basename + "-25",
    dockerBaseImage := "eclipse-temurin:25",
    dockerBuildOptions := dockerBuildOptions.value ++ Seq("-t", "jdk-versions:25")
  )
