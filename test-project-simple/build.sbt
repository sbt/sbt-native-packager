name := "test-project-simple"
version := "0.2.0"
libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.2.1"
)

mainClass in Compile := Some("ExampleApp")

enablePlugins(JavaAppPackaging, JDebPackaging)

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"
packageSummary := "Minimal Native Packager"
packageDescription := """A fun package description of our software,
  with multiple lines."""

// RPM SETTINGS
rpmVendor := "typesafe"
rpmLicense := Some("BSD")
rpmChangelogFile := Some("changelog.txt")

// these settings are conflicting
javaOptions in Universal ++= Seq(
  "-J-Xmx64m", "-J-Xms64m"
)

//bashScriptConfigLocation := Some("${app_home}/../conf/jvmopts")