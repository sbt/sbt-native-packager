sbtPlugin := true

name := "sbt-native-packager"
organization := "com.typesafe.sbt"

scalaVersion in Global := "2.10.5"
scalacOptions in Compile ++= Seq("-deprecation", "-target:jvm-1.7")

libraryDependencies ++= Seq(
    "org.apache.commons" % "commons-compress" % "1.4.1",
    // these dependencies have to be explicitly added by the user
    "com.spotify" % "docker-client" % "3.2.1" % "provided",
    "org.vafer" % "jdeb" % "1.3"  % "provided" artifacts (Artifact("jdeb", "jar", "jar")),
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

// configure github page
site.settings

com.typesafe.sbt.SbtSite.SiteKeys.siteMappings <+= (baseDirectory) map { dir => 
  val nojekyll = dir / "src" / "site" / ".nojekyll"
  nojekyll -> ".nojekyll"
}
site.sphinxSupport()
site.includeScaladoc()
ghpages.settings
git.remoteRepo := "git@github.com:sbt/sbt-native-packager.git"

// scripted test settings
scriptedSettings
scriptedLaunchOpts <+= version apply { v => "-Dproject.version="+v }

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
  pushChanges
)

// bintray config
bintrayOrganization := Some("sbt")
bintrayRepository := "sbt-plugin-releases"

// scalariform
import scalariform.formatter.preferences._
scalariformSettings
ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignParameters, false)
  .setPreference(FormatXml, true)
  .setPreference(SpaceInsideBrackets, false)
  .setPreference(IndentWithTabs, false)
  .setPreference(SpaceInsideParentheses, false)
  .setPreference(MultilineScaladocCommentsStartOnFirstLine, false)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(CompactStringConcatenation, false)
  .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, false)
  .setPreference(IndentPackageBlocks, true)
  .setPreference(CompactControlReadability, false)
  .setPreference(SpacesWithinPatternBinders, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 40)
  .setPreference(DoubleIndentClassDeclaration, false)
  .setPreference(PreserveSpaceBeforeArguments, false)
  .setPreference(SpaceBeforeColon, false)
  .setPreference(RewriteArrowSymbols, false)
  .setPreference(IndentLocalDefs, false)
  .setPreference(IndentSpaces, 2)

