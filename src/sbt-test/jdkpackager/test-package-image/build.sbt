enablePlugins(JDKPackagerPlugin)

name := "JDKPackagerPluginTest"

version := "0.1.0"

organization := "com.foo.bar"

mainClass in Compile := Some("ExampleApp")

maintainer := "Simeon H.K Fitch <fitch@datamininglab.com>"

packageSummary := "test-jdkpackager"

packageDescription := "Test JDKPackagerPlugin"

jdkPackagerType := "image"

TaskKey[Unit]("checkImage") <<= (target in JDKPackager, name, streams) map { (base, name, streams) ⇒
    val extension = sys.props("os.name").toLowerCase match {
        case os if os.contains("mac") ⇒ ".app"
        case os if os.contains("win") ⇒ ".exe"
        case _ ⇒ ""
    }
    val expectedImage = base / "bundles" / (name + extension)
    assert(expectedImage.exists, s"Expected image file to be found at '$expectedImage'")
}
