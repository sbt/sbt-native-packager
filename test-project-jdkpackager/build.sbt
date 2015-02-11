import com.typesafe.sbt.packager.jdkpackager.JDKPackagerPlugin
name := "test-project-jdkpackager"

version := "0.1.0"

libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.2.1"
)

mainClass in Compile := Some("ExampleApp")

enablePlugins(JDKPackagerPlugin)

maintainer := "Simeon H.K Fitch <fitch@datamininglab.com>"

packageSummary := "JDK JavaPackager Package"

packageDescription :=
    """A test project
      |generating a package using
      |Oracle's `javapackager` tool bundled with the
      |JDK""".stripMargin

