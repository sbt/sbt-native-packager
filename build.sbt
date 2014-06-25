import scalariform.formatter.preferences._

sbtPlugin := true

sbtVersion in Global := {
  scalaBinaryVersion.value match {
    case "2.10" => "0.13.0"
    case "2.9.2" => "0.12.4"
  }
}

scalaVersion in Global := "2.10.2"

crossScalaVersions := Seq("2.9.2", "2.10.2")

name := "sbt-native-packager"

organization := "com.typesafe.sbt"

scalacOptions in Compile += "-deprecation"

libraryDependencies ++= Seq(
    "org.apache.commons" % "commons-compress" % "1.4.1",
    "org.vafer" % "jdeb" % "1.2"
)

site.settings

com.typesafe.sbt.SbtSite.SiteKeys.siteMappings <+= (baseDirectory) map { dir => 
  val nojekyll = dir / "src" / "site" / ".nojekyll"
  nojekyll -> ".nojekyll"
}

site.sphinxSupport()

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

