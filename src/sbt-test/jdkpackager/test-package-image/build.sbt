enablePlugins(JDKPackagerPlugin)

name := "JDKPackagerPluginTest"

version := "0.1.0"

organization := "com.foo.bar"

Compile / mainClass := Some("ExampleApp")

maintainer := "Previously Owned Cats, Inc."

packageSummary := "test-jdkpackager"

packageDescription := "Test JDKPackagerPlugin"

jdkPackagerType := "image"

jdkPackagerToolkit := JavaFXToolkit

jdkPackagerJVMArgs := Seq("-Xmx1g", "-Xdiag")

jdkPackagerProperties := Map("app.name" -> name.value, "app.version" -> version.value)

jdkPackagerAppArgs := Seq(maintainer.value, packageSummary.value, packageDescription.value)

jdkPackagerAssociations := Seq(
  FileAssociation("foobar", "application/foobar", "Foobar file type"),
  FileAssociation("barbaz", "application/barbaz", "Barbaz file type", jdkAppIcon.value)
)

lazy val iconGlob = sys.props("os.name").toLowerCase match {
  case os if os.contains("mac") => "*.icns"
  case os if os.contains("win") => "*.ico"
  case _                        => "*.png"
}

jdkAppIcon := (baseDirectory.value / ".." / ".." / ".." / ".." / "test-project-jdkpackager" ** iconGlob).getPaths().headOption
  .map(file)

TaskKey[Unit]("checkImage") := {
  val (extension, os) = sys.props("os.name").toLowerCase match {
    case osys if osys.contains("mac") => ("", "mac")
    case osys if osys.contains("win") => (".exe", "windows")
    case _                            => ("", "linux")
  }
  val expectedImage = (JDKPackager / target).value / "bundles" / (name.value + extension)
  println(s"Checking for '${expectedImage.getAbsolutePath}'")
  assert(expectedImage.exists, s"Expected image file to be found at '$expectedImage'")

// TODO: Inspect the generated bundle, confirming that JVM args and app args are passed.
}
