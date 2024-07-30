scalaVersion in ThisBuild := "2.11.6"

scalacOptions in ThisBuild ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-unchecked", "-Xfuture", "-Xlint")

name := "test-project-play-rpm"

description := "Demo of RPM packaging"

libraryDependencies ++= Seq("com.typesafe.play" %% "play" % "2.3.8")

enablePlugins(PlayScala)

enablePlugins(RpmPlugin)
