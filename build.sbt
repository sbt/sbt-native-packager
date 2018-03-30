sbtPlugin := true

name := "sbt-native-packager"
organization := "com.typesafe.sbt"

Global / scalaVersion := "2.12.5"

// crossBuildingSettings
crossSbtVersions := Vector("0.13.17", "1.1.1")

Compile / scalacOptions ++= Seq("-deprecation")
javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

// put jdeb on the classpath for scripted tests
classpathTypes += "maven-plugin"
libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-compress" % "1.14",
  // for jdkpackager
  "org.apache.ant" % "ant" % "1.10.1",
  "org.scalatest" %% "scalatest" % "3.0.3" % Test
)

// sbt dependend libraries
libraryDependencies ++= {
  (pluginCrossBuild / sbtVersion).value match {
    case v if v.startsWith("1.") =>
      Seq(
        "org.scala-sbt" %% "io" % "1.0.0",
        // these dependencies have to be explicitly added by the user
        // FIXME temporary remove the 'provided' scope. SBT 1.0.0-M6 changed the resolving somehow
        "com.spotify" % "docker-client" % "8.9.0" /* % "provided" */,
        "org.vafer" % "jdeb" % "1.3" /*% "provided"*/ artifacts Artifact("jdeb", "jar", "jar")
      )
    case _ =>
      Seq(
        // these dependencies have to be explicitly added by the user
        "com.spotify" % "docker-client" % "8.9.0" % Provided,
        "org.vafer" % "jdeb" % "1.3" % Provided artifacts Artifact("jdeb", "jar", "jar")
      )
  }
}

// scala version depended libraries
libraryDependencies ++= {
  scalaBinaryVersion.value match {
    case "2.10" => Nil
    case _ =>
      Seq(
        "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6",
        "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
      )
  }
}

// configure github page
enablePlugins(SphinxPlugin, SiteScaladocPlugin, GhpagesPlugin)

git.remoteRepo := "git@github.com:sbt/sbt-native-packager.git"

// scripted test settings
scriptedLaunchOpts += "-Dproject.version=" + version.value

// Release configuration
releasePublishArtifactsAction := PgpKeys.publishSigned.value
publishMavenStyle := false

// The release task doesn't run any tests. We rely on travis.ci and appveyor,
// because it's impossible to run all tests (linux, macosx, windows) on a single computer.
import ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  updateReadme,
  commitReadme,
  releaseStepCommandAndRemaining("^ publishSigned"),
  setNextVersion,
  commitNextVersion,
  pushChanges,
  generateReleaseChangelog,
  commitChangelog,
  pushChanges,
  releaseStepTask(ghpagesPushSite)
)

// bintray config
bintrayOrganization := Some("sbt")
bintrayRepository := "sbt-plugin-releases"

addCommandAlias("scalafmtAll", "; scalafmt ; test:scalafmt ; sbt:scalafmt")
// ci commands
addCommandAlias("validateFormatting", "; scalafmt::test ; test:scalafmt::test ; sbt:scalafmt::test")
addCommandAlias("validate", "; clean ; update ; validateFormatting ; test")

// List all scripted test separately to schedule them in different travis-ci jobs.
// Travis-CI has hard timeouts for jobs, so we run them in smaller jobs as the scripted
// tests take quite some time to run.
// Ultimatley we should run only those tests that are necessary for a change
addCommandAlias("validateUniversal", "scripted universal/*")
addCommandAlias("validateJar", "scripted jar/*")
addCommandAlias("validateBash", "scripted bash/*")
addCommandAlias("validateAsh", "scripted ash/*")
addCommandAlias("validateRpm", "scripted rpm/*")
addCommandAlias("validateDebian", "scripted debian/*")
addCommandAlias("validateDocker", "scripted docker/*")
addCommandAlias("validateDockerUnit", "scripted docker/staging docker/entrypoint docker/ports docker/volumes")
addCommandAlias("validateJdkPackager", "scripted jdkpackager/*")
// travis ci's jdk8 version doesn't support nested association elements.
// error: Caused by: class com.sun.javafx.tools.ant.Info doesn't support the nested "association" element.
addCommandAlias(
  "validateJdkPackagerTravis",
  "scripted jdkpackager/test-package-minimal jdkpackager/test-package-mappings"
)
addCommandAlias("validateOSX", "; validate ; validateUniversal")

// TODO check the cygwin scripted tests and run them on appveyor
addCommandAlias("validateWindows", "; testOnly * -- -n windows ; scripted universal/dist universal/stage windows/*")
