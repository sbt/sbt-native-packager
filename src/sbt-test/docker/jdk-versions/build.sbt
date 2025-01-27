val basename = "jdk-versions"

ThisBuild / Compile / compile / scalacOptions := Seq("-target:jvm-1.8")

lazy val `jdk8` = project
  .in(file("jdk8"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := basename + "-8",
    dockerBaseImage := "openjdk:8u162-jre",
    dockerBuildOptions := dockerBuildOptions.value ++ Seq("-t", "jdk-versions:8")
  )

lazy val `jdk9` = project
  .in(file("jdk9"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := basename + "-9",
    dockerBaseImage := "openjdk:9.0.4-jre",
    dockerBuildOptions := dockerBuildOptions.value ++ Seq("-t", "jdk-versions:9")
  )

lazy val `jdk10` = project
  .in(file("jdk10"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := basename + "-10",
    dockerBaseImage := "openjdk:10-jre",
    dockerBuildOptions := dockerBuildOptions.value ++ Seq("-t", "jdk-versions:10")
  )
