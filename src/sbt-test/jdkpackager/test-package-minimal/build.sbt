// Tests plugin behavior when none of the metadata keys are set.

enablePlugins(JDKPackagerPlugin)

mainClass in Compile := Some("ExampleApp")

TaskKey[Unit]("checkImage") <<= (target in JDKPackager, name, streams) map { (base, name, streams) ⇒
    val extension = sys.props("os.name").toLowerCase match {
        case os if os.contains("mac") ⇒ ".app"
        case os if os.contains("win") ⇒ ".exe"
        case _ ⇒ ""
    }
    val expectedImage = base / "bundles" / (name + extension)
    println(s"Checking for '${expectedImage.getAbsolutePath}'")
    assert(expectedImage.exists, s"Expected image file to be found at '$expectedImage'")
}
