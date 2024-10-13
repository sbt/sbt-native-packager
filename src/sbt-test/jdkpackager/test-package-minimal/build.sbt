// Tests plugin behavior when none of the metadata keys are set.

enablePlugins(JDKPackagerPlugin)

Compile / mainClass := Some("ExampleApp")

jdkPackagerType := "image"

TaskKey[Unit]("checkImage") := {
  val extension = sys.props("os.name").toLowerCase match {
    case os if os.contains("mac") ⇒ ".app"
    case os if os.contains("win") ⇒ ".exe"
    case _ ⇒ ""
  }
  val expectedImage = (JDKPackager / target).value / "bundles" / (name.value + extension)
  println(s"Checking for '${expectedImage.getAbsolutePath}'")
  assert(expectedImage.exists, s"Expected image file to be found at '$expectedImage'")
}
