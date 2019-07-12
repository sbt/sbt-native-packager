// Various JlinkPlugin test cases that don't warrant setting up separate
// `scripted` tests.

import scala.sys.process.Process
import com.typesafe.sbt.packager.Compat._

val runChecks = taskKey[Unit]("Run checks for a specific issue")

// Exclude Scala by default to simplify the test.
autoScalaLibrary in ThisBuild := false

// Should succeed for multi-release artifacts
val issue1243 = project
  .enablePlugins(JlinkPlugin)
  .settings(
    libraryDependencies ++= List(
      // An arbitrary multi-release artifact
      "org.apache.logging.log4j" % "log4j-core" % "2.12.0"
    ),
    // Don't bother with providing dependencies.
    jlinkIgnoreMissingDependency := JlinkIgnore.everything,
    runChecks := jlinkBuildImage.value
  )

// Should succeed for JARs with names that don't produce a legal automatic
// module name.
val issue1247BadAutoModuleName = project
  .enablePlugins(JlinkPlugin)
  .settings(
    managedClasspath in Compile += {
      // Build an empty jar with an unsupported name
      val jarFile = target.value / "foo_2.11.jar"
      IO.jar(Nil, jarFile, new java.util.jar.Manifest)
      Attributed.blank(jarFile)
    },
    runChecks := jlinkBuildImage.value
  )

// Should succeed for jars containing external modules.
val issue1247ExternalModule = project
  .enablePlugins(JlinkPlugin)
  .settings(
    // An arbitrary JAR with a non-platform module.
    libraryDependencies += "com.sun.xml.fastinfoset" % "FastInfoset" % "1.2.16",
    runChecks := jlinkBuildImage.value
  )

// Should succeed for `java.*` modules from JakartaEE.
val issue1247JakartaJavaModules = project
  .enablePlugins(JlinkPlugin)
  .settings(
    libraryDependencies ++= List(
      "jakarta.ws.rs" % "jakarta.ws.rs-api" % "2.1.5",
      "jakarta.xml.bind" % "jakarta.xml.bind-api" % "2.3.2",

      // We don't use the implementation from
      // `com.sun.activation` because that doesn't include an explicit module
      // declaration, and automatic modules are not properly supported
      // with jdeps 11+.
      // https://github.com/eclipse-ee4j/jaf/issues/13
      "com.jwebmp.thirdparty" % "jakarta.activation" % "0.67.0.12"
    ),
    runChecks := jlinkBuildImage.value
  )
