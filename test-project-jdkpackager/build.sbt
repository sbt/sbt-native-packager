
name := "JDKPackagerPlugin Example"

version := "0.1.0"

organization := "com.foo.bar"

libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.2.1"
)

mainClass in Compile := Some("ExampleApp")

enablePlugins(JDKPackagerPlugin)

maintainer := "Simeon H.K Fitch <fitch@datamininglab.com>"

packageSummary := "JDKPackagerPlugin example package thingy"

packageDescription := "A test package using Oracle's JDK bundled javapackager tool."

lazy val iconGlob = sys.props("os.name").toLowerCase match {
  case os if os.contains("mac") ⇒ "*.icns"
  case os if os.contains("win") ⇒ "*.ico"
  case _ ⇒ "*.png"
}

jdkAppIcon :=  (sourceDirectory.value ** iconGlob).getPaths.headOption.map(file)

jdkPackagerType := "installer"

fork := true
