scalaVersion := "2.13.18"

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-unchecked", "-Xfuture", "-Xlint")

name := "test-project-play-rpm"

description := "Demo of RPM packaging"

libraryDependencies += guice

enablePlugins(PlayScala)

enablePlugins(RpmPlugin)
