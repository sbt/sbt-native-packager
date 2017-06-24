sbtPlugin := true

name := "sbt-native-packager"
organization := "com.typesafe.sbt"

scalaVersion in Global := "2.10.6"

// crossBuildingSettings
crossSbtVersions := Vector("0.13.15", "1.0.0-M6")

scalacOptions in Compile ++= Seq("-deprecation")
javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

// put jdeb on the classpath for scripted tests
classpathTypes += "maven-plugin"
libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-compress" % "1.4.1",
  // for jdkpackager
  "org.apache.ant" % "ant" % "1.9.6",
  // these dependencies have to be explicitly added by the user
  // FIXME temporary remove the 'provided' scope. SBT 1.0.0-M6 changed the resolving somehow
  "com.spotify" % "docker-client" % "3.5.13" /* % "provided" */,
  // FIXME temporary remove the 'provided' scope. SBT 1.0.0-M6 changed the resolving somehow
  "org.vafer" % "jdeb" % "1.3" /*% "provided"*/ artifacts Artifact("jdeb", "jar", "jar"),
  "org.scalatest" %% "scalatest" % "3.0.3" % "test"
)

// sbt dependend libraries
libraryDependencies ++= {
  sbtVersion.value match {
    case v if v.startsWith("1.") => Seq("org.scala-sbt" %% "io" % "1.0.0-M11")
    case _                       => Nil
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
enablePlugins(SphinxPlugin, SiteScaladocPlugin)

ghpages.settings
git.remoteRepo := "git@github.com:sbt/sbt-native-packager.git"

// scripted test settings
scriptedSettings
scriptedLaunchOpts += "-Dproject.version=" + version.value

// Temporary fix for issue sbt/sbt/issues/3245
scripted := {
  val args = ScriptedPlugin
    .asInstanceOf[{
	def scriptedParser(f: File): complete.Parser[Seq[String]]
      }
    ]
    .scriptedParser(sbtTestDirectory.value)
    .parsed
  val prereq: Unit = scriptedDependencies.value
  try {
    if ((sbtVersion in pluginCrossBuild).value == "1.0.0-M6") {
      ScriptedPlugin.scriptedTests.value
	.asInstanceOf[{
	    def run(x1: File, x2: Boolean, x3: Array[String], x4: File, x5: Array[String], x6: java.util.List[File])
	      : Unit
	  }
	]
	.run(
	  sbtTestDirectory.value,
	  scriptedBufferLog.value,
	  args.toArray,
	  sbtLauncher.value,
	  scriptedLaunchOpts.value.toArray,
	  new java.util.ArrayList()
	)
    } else {
      ScriptedPlugin.scriptedTests.value
	.asInstanceOf[{
	    def run(x1: File, x2: Boolean, x3: Array[String], x4: File, x5: Array[String]): Unit
	  }
	]
	.run(
	  sbtTestDirectory.value,
	  scriptedBufferLog.value,
	  args.toArray,
	  sbtLauncher.value,
	  scriptedLaunchOpts.value.toArray
	)
    }
  } catch {
    case e: java.lang.reflect.InvocationTargetException => throw e.getCause
  }
}

// Release configurationr
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
addCommandAlias(
  "validate",
  "; update ; test ; scripted jdkpackager/test-package-minimal jdkpackager/test-package-mappings"
)
addCommandAlias("validateUnix", "scripted universal/* jar/* bash/* ash/*")

addCommandAlias("validateOSX", "; validate ; validateUnix")
addCommandAlias(
  "validateLinux",
  "; validate ; validateUnix ; scripted rpm/* debian/* docker/staging docker/entrypoint docker/ports docker/volumes"
)
addCommandAlias("validateWindows", ";test-only * -- -n windows;scripted universal/dist universal/stage windows/*")
