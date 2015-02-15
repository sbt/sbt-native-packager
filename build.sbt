import scalariform.formatter.preferences._

sbtPlugin := true

scalaVersion in Global := "2.10.2"

name := "sbt-native-packager"

organization := "com.typesafe.sbt"

scalacOptions in Compile ++= Seq("-deprecation", "-target:jvm-1.6")

libraryDependencies ++= Seq(
    "org.apache.commons" % "commons-compress" % "1.4.1",
    "org.vafer" % "jdeb" % "1.3" artifacts (Artifact("jdeb", "jar", "jar")),
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

site.settings

com.typesafe.sbt.SbtSite.SiteKeys.siteMappings <+= (baseDirectory) map { dir => 
  val nojekyll = dir / "src" / "site" / ".nojekyll"
  nojekyll -> ".nojekyll"
}

site.sphinxSupport()

site.includeScaladoc()

ghpages.settings

git.remoteRepo := "git@github.com:sbt/sbt-native-packager.git"

Bintray.settings

publishMavenStyle := false

scriptedSettings

scriptedLaunchOpts <+= version apply { v => "-Dproject.version="+v }

Release.settings

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
  //.setPreference(AreserveDanglingCloseParenthesis, true)

