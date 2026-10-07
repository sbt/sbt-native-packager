import com.typesafe.tools.mima.core.{DirectMissingMethodProblem, ProblemFilters}

name := "sbt-native-packager"
organization := "com.github.sbt"
homepage := Some(url("https://github.com/sbt/sbt-native-packager"))

Global / onChangedBuildSource := ReloadOnSourceChanges

val sbt2 = "2.0.10"
// crossBuildingSettings
lazy val scala212 = "2.12.21"
lazy val scala3 = scala_version_from_sbt_version.ScalaVersionFromSbtVersion(sbt2)
Global / scalaVersion := scala212
crossScalaVersions := Seq(scala3, scala212)
(pluginCrossBuild / sbtVersion) := {
  scalaBinaryVersion.value match {
    case "2.12" => "1.5.8"
    case _      => sbt2
  }
}
scriptedSbt := {
  scalaBinaryVersion.value match {
    case "2.12" => "1.10.7"
    case _      => sbt2
  }
}

Compile / scalacOptions ++= Seq("-deprecation")
scalacOptions ++= {
  scalaBinaryVersion.value match {
    case "2.12" =>
      Seq("-release:8")
    case "3" =>
      Seq("-Wconf:msg=is excluded from the cache input:error")
  }
}
javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

// put jdeb on the classpath for scripted tests
classpathTypes += "maven-plugin"
addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.1.0")

libraryDependencies ++= Seq(
  // these dependencies have to be explicitly added by the user
  "com.spotify" % "docker-client" % "8.16.0" % Provided,
  ("org.vafer" % "jdeb" % "1.14" % Provided).artifacts(Artifact("jdeb", "jar", "jar")),
  "org.apache.commons" % "commons-compress" % "1.28.0",
  // for jdkpackager
  "org.apache.ant" % "ant" % "1.10.18",
  // workaround for the command line size limit
  "com.github.eldis" % "tool-launcher" % "0.2.2",
  "org.scalatest" %% "scalatest-wordspec" % "3.2.20" % Test,
  "org.scalatest" %% "scalatest-flatspec" % "3.2.20" % Test,
  "org.scalatest" %% "scalatest-diagrams" % "3.2.20" % Test,
  "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.20" % Test
)

// sbt dependent libraries
libraryDependencies ++= {
  (pluginCrossBuild / sbtVersion).value match {
    case v if v.startsWith("1.") =>
      Seq("org.scala-sbt" %% "io" % "1.13.5")
    case _ => Seq()
  }
}

// configure github page
enablePlugins(SphinxPlugin, SiteScaladocPlugin, GhpagesPlugin, SbtPlugin)

git.remoteRepo := {
  sys.env.get("GITHUB_TOKEN") match {
    case Some(token) => s"https://x-access-token:$token@github.com/sbt/sbt-native-packager"
    case None        => "git@github.com:sbt/sbt-native-packager.git"
  }
}

// scripted test settings
scriptedLaunchOpts += "-Dproject.version=" + version.value

// binary compatibility settings
mimaPreviousArtifacts := {
  val m = organization.value %% moduleName.value % "1.12.0"
  val sbtBinV = (pluginCrossBuild / sbtBinaryVersion).value
  val scalaBinV = (update / scalaBinaryVersion).value
  Set(Defaults.sbtPluginExtra(m, sbtBinV, scalaBinV))
}

mimaBinaryIssueFilters ++= Seq(
  "com.typesafe.sbt.packager.Keys.debianMaintainerScripts()sbt.TaskKey",
  "com.typesafe.sbt.packager.Keys.wixFile()sbt.TaskKey",
  "com.typesafe.sbt.packager.debian.DebianKeys.debianMaintainerScripts()sbt.TaskKey",
  "com.typesafe.sbt.packager.debian.DebianPlugin#autoImport.debianMaintainerScripts()sbt.TaskKey",
  "com.typesafe.sbt.packager.windows.WindowsKeys.wixFile()sbt.TaskKey",
  "com.typesafe.sbt.packager.windows.WindowsPlugin#autoImport.wixFile()sbt.TaskKey"
).map(ProblemFilters.exclude[DirectMissingMethodProblem])

// Release configuration
publishMavenStyle := true
// project meta data
licenses := Seq(License.Apache2)
homepage := Some(url("https://github.com/sbt/sbt-native-packager"))

scmInfo := Some(
  ScmInfo(url("https://github.com/sbt/sbt-native-packager"), "scm:git@github.com:sbt/sbt-native-packager.git")
)
developers := List(
  Developer(
    id = "muuki88",
    name = "Nepomuk Seiler",
    email = "nepomuk.seiler@gmail.com",
    url = url("https://github.com/muuki88")
  ),
  Developer(id = "jsuereth", name = "Josh Suereth", email = "jsuereth", url = url("https://github.com/jsuereth"))
)

// ci commands
addCommandAlias("validateFormatting", "; scalafmtCheckRepo")
addCommandAlias("validate", "; clean ; update ; validateFormatting ; test ; mimaReportBinaryIssues")

// List all scripted test separately to schedule them in different travis-ci jobs.
// Travis-CI has hard timeouts for jobs, so we run them in smaller jobs as the scripted
// tests take quite some time to run.
// Ultimately we should run only those tests that are necessary for a change
addCommandAlias("validateUniversal", "scripted universal/*")
addCommandAlias("validateUniversalSbt1", "++ 2.12.x; scripted universal-sbt1/*")
addCommandAlias("validateUniversalSbt2", "++ 3.x; scripted universal-sbt2/*")
addCommandAlias("validateJar", "scripted jar/*")
addCommandAlias("validateBash", "scripted bash/*")
addCommandAlias("validateAsh", "scripted ash/*")
addCommandAlias("validateGraalVMNativeImage", "scripted graalvm-native-image/*")
addCommandAlias("validateRpm", "scripted rpm/*")
addCommandAlias("validateDebian", "scripted debian/*")
addCommandAlias("validateDocker", "scripted docker/*")
addCommandAlias("validateDockerSbt1", "++ 2.12.x; scripted docker-sbt1/*")
addCommandAlias("validateDockerSbt2", "++ 3.x; scripted docker-sbt2/*")
addCommandAlias("validateJdkPackager", "scripted jdkpackager/*")
// travis ci's jdk8 version doesn't support nested association elements.
// error: Caused by: class com.sun.javafx.tools.ant.Info doesn't support the nested "association" element.
addCommandAlias(
  "validateJdkPackagerTravis",
  "scripted jdkpackager/test-package-minimal jdkpackager/test-package-mappings"
)
addCommandAlias("validateMacOS", "; validate ; validateUniversal")

addCommandAlias("validateWindows", "; testOnly * -- -n windows ; scripted universal/dist universal/stage windows/*")

addCommandAlias("validateJlink", "scripted jlink/*")

addCommandAlias("ci-release", "release with-defaults")

// So that publishLocal doesn't continuously create new versions
def versionFmt(out: sbtdynver.GitDescribeOutput): String = {
  val snapshotSuffix =
    if (out.isSnapshot()) "-SNAPSHOT"
    else ""
  dropBackPubCommand(out.ref.dropPrefix) + snapshotSuffix
}
def dropBackPubCommand(ver: String): String = {
  val nonComment =
    if (ver.contains("#")) ver.split("#").head
    else ver
  if (nonComment.contains("@")) nonComment.split("@").head
  else nonComment
}
def fallbackVersion(d: java.util.Date): String = s"HEAD-${sbtdynver.DynVer.timestamp(d)}"

ThisBuild / version := {
  val orig = (ThisBuild / version).value
  if ((ThisBuild / isSnapshot).value)
    dynverGitDescribeOutput.value.mkVersion(versionFmt, fallbackVersion(dynverCurrentDate.value))
  else orig
}

ThisBuild / dynver := {
  val d = new java.util.Date
  sbtdynver.DynVer.getGitDescribeOutput(d).mkVersion(versionFmt, fallbackVersion(d))
}
