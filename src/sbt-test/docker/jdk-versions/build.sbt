val basename = "jdk-versions"

ThisBuild / Compile / compile / scalacOptions := Seq("-target:jvm-1.8")

lazy val `jdk17` = project
  .in(file("jdk17"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := basename + "-17",
    dockerBaseImage := "eclipse-temurin:17-jre",
    dockerBuildOptions := dockerBuildOptions.value ++ Seq("-t", "jdk-versions:17")
  )

lazy val `jdk21` = project
  .in(file("jdk21"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := basename + "-21",
    dockerBaseImage := "eclipse-temurin:21-jre",
    dockerBuildOptions := dockerBuildOptions.value ++ Seq("-t", "jdk-versions:21")
  )

lazy val `jdk25` = project
  .in(file("jdk25"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := basename + "-25",
    dockerBaseImage := "eclipse-temurin:25-jre",
    dockerBuildOptions := dockerBuildOptions.value ++ Seq("-t", "jdk-versions:25")
  )
