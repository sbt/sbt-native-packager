name := "test-layer-groups-playframework"

ThisBuild / organization := "com.example"
ThisBuild / version := "1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file(".")).enablePlugins(PlayScala).dependsOn(common)

// a local project dependency
lazy val common = project.settings(
  name := "test-layer-groups-playframework-common",

  // a transitive dependency of the main project
  // (use any library as long as it's not already a Play dependency)
  libraryDependencies += "org.typelevel" %% "cats-core" % "2.13.0"
)

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"

enablePlugins(DockerPlugin, LauncherJarPlugin)

// generate a conf/application.ini
Universal / javaOptions ++= Seq("-J-Xms1024m", "-J-Xmx1024m")
