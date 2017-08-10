enablePlugins(JDKPackagerPlugin)

name := "JDKPackagerPluginTest"

version := "0.1.1"

organization := "com.foo.bar"

mainClass in Compile := Some("ExampleApp")

maintainer := "Cat D. Herder"

packageSummary := "test-jdkpackager"

packageDescription := "Test JDKPackagerPlugin with mappings"

jdkPackagerType := "image"

mappings in Universal += baseDirectory.value / "src" / "deploy" / "README.md" -> "README.md"

mappings in Universal ++= {
  val dir = baseDirectory.value / "src" / "deploy" / "stuff"
  (dir.**(AllPassFilter) --- dir) pair (file => IO.relativize(dir.getParentFile, file))
}

lazy val iconGlob = sys.props("os.name").toLowerCase match {
  case os if os.contains("mac") ⇒ "*.icns"
  case os if os.contains("win") ⇒ "*.ico"
  case _ ⇒ "*.png"
}

jdkAppIcon := (baseDirectory.value / ".." / ".." / ".." / ".." / "test-project-jdkpackager" ** iconGlob).getPaths.headOption
  .map(file)

TaskKey[Unit]("checkImage") := {
  val (extension, os) = sys.props("os.name").toLowerCase match {
    case osys if osys.contains("mac") ⇒ (".app", 'mac)
    case osys if osys.contains("win") ⇒ (".exe", 'windows)
    case _ ⇒ ("", 'linux)
  }
  val expectedImage = (target in JDKPackager).value / "bundles" / (name.value + extension)
  println(s"Checking for '${expectedImage.getAbsolutePath}'")
  assert(expectedImage.exists, s"Expected image file to be found at '$expectedImage'")

  val files = os match {
    case 'mac ⇒
      Seq(
        expectedImage / "Contents" / "Java" / "README.md",
        expectedImage / "Contents" / "Java" / "stuff" / "something-1.md",
        expectedImage / "Contents" / "Java" / "stuff" / "something-2.md"
      )
    case _ ⇒
      streams.value.log.warn("Test needs to be implemented for " + sys.props("os.name"))
      Seq.empty
  }

  println(s"Checking for ${files.map(_.getName).mkString(", ")}")
  files.foreach(f ⇒ assert(f.exists, "Expected to find " + f))
}
