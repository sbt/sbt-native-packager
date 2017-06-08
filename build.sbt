sbtPlugin := true

name := "sbt-native-packager"
organization := "com.typesafe.sbt"

scalaVersion in Global := "2.10.5"
scalacOptions in Compile ++= Seq("-deprecation", "-target:jvm-1.7")

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-compress" % "1.4.1",
  // for jdkpackager
  "org.apache.ant" % "ant" % "1.9.6",
  // these dependencies have to be explicitly added by the user
  "com.spotify" % "docker-client" % "3.5.13" % "provided",
  "org.vafer" % "jdeb" % "1.3" % "provided" artifacts (Artifact("jdeb", "jar", "jar")),
  "org.scalatest" %% "scalatest" % "3.0.3" % "test"
)

// configure github page
enablePlugins(SphinxPlugin, SiteScaladocPlugin)

ghpages.settings
git.remoteRepo := "git@github.com:sbt/sbt-native-packager.git"

// scripted test settings
scriptedSettings
scriptedLaunchOpts += "-Dproject.version=" + version.value

// Release configuration
releasePublishArtifactsAction := PgpKeys.publishSigned.value
publishMavenStyle := false

import ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  releaseStepInputTask(scripted, " universal/* debian/* rpm/* docker/* ash/* jar/* bash/* jdkpackager/*"),
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges,
  releaseStepTask(GhPagesKeys.pushSite)
)

// bintray config
bintrayOrganization := Some("sbt")
bintrayRepository := "sbt-plugin-releases"

// scalafmt
scalafmtConfig := Some(file(".scalafmt.conf"))

// ci commands
addCommandAlias("validateWindows", ";test-only * -- -n windows;scripted universal/dist universal/stage windows/*")
