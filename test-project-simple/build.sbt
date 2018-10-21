name := "test-project-simple"
version := "0.2.0"
libraryDependencies ++= Seq("com.typesafe" % "config" % "1.2.1")

enablePlugins(JavaServerAppPackaging, UpstartPlugin, GraalVMNativeImagePlugin)

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"
packageSummary := "Minimal Native Packager"
packageDescription := """A fun package description of our software,
  with multiple lines."""

// RPM SETTINGS
rpmVendor := "typesafe"
rpmLicense := Some("BSD")
rpmChangelogFile := Some("changelog.txt")

// these settings are conflicting
javaOptions in Universal ++= Seq("-J-Xmx64m", "-J-Xms64m", "-jvm-debug 12345")

//bashScriptConfigLocation := Some("${app_home}/../conf/jvmopts")

mappings in UniversalSrc := (mappings in Universal).value

maintainer in Universal := ""
mappings in Universal ++= Seq(
  (baseDirectory.value / "foo.txt") -> "foo.txt",
  (baseDirectory.value / "bar.txt") -> "bar.txt"
)