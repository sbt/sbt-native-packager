// Various JlinkPlugin test cases that don't warrant setting up separate
// `scripted` tests.

import scala.sys.process.Process
import com.typesafe.sbt.packager.Compat._
import com.typesafe.sbt.packager.PluginCompat
import xsbti.FileConverter

val runChecks = taskKey[Unit]("Run checks for a specific issue")
val runFailingChecks = taskKey[Unit]("Run checks for a specific issue, expecting them to fail")

// Exclude Scala by default to simplify the test.
ThisBuild / autoScalaLibrary := false

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
    Compile / managedClasspath += {
      implicit val converter: FileConverter = fileConverter.value
      // Build an empty jar with an unsupported name
      val jarFile = target.value / "foo_2.11.jar"
      IO.jar(Nil, jarFile, new java.util.jar.Manifest)
      Attributed.blank(PluginCompat.toFileRef(jarFile))
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

// Should succeed for large classpaths.
val issue1266 = project
  .enablePlugins(JlinkPlugin)
  .settings(
    // An arbitrary JAR with a non-platform module.
    libraryDependencies += "com.sun.xml.fastinfoset" % "FastInfoset" % "1.2.16",
    // A lot of "dummy" dependencies, so that the resulting classpath is over
    // the command line limit (2MB on my machine)
    jlinkModulePath ++= {
      def mkPath(ix: Int) = target.value / s"there-is-no-such-file-$ix.jar"

      1.to(300000).map(mkPath)
    },
    jlinkModules / logLevel := Level.Error,
    runChecks := jlinkBuildImage.value
  )

// Should fail for invalid jlink inputs
val issue1284 = project
  .enablePlugins(JlinkPlugin)
  .settings(jlinkModules := List("no-such-module"), runFailingChecks := jlinkBuildImage.value)

// We should be able to make the whole thing work for modules that depend
// on automatic modules - at least by manually setting `jlinkModulePath`.
val issue1293 = project
  .enablePlugins(JlinkPlugin)
  .settings(
    libraryDependencies ++= Seq(
      // This has a module dependency on `paranamer`, which is not an explicit module.
      "com.fasterxml.jackson.module" % "jackson-module-paranamer" % "2.10.1",
      // Make sure that JARs with "bad" names are excluded.
      "org.typelevel" % "cats-core_2.12" % "2.0.0",

      // Two dependencies with overlapping packages - just to make sure we don't inadvertedly
      // put them into the module path.
      "org.openoffice" % "unoil" % "4.1.2",
      "org.openoffice" % "ridl" % "4.1.2"
    ),
    jlinkIgnoreMissingDependency := JlinkIgnore.everything,
    // Use `paramaner` (and only it) as an automatic module
    jlinkModulePath := {
      implicit val converter: FileConverter = fileConverter.value
      // Get the full classpath with all the resolved dependencies.
      (jlinkBuildImage / fullClasspath).value
        // Find the ones that have `paranamer` as their artifact names.
        .filter { item =>
          item.get(PluginCompat.moduleIDStr)
            .map(PluginCompat.parseModuleIDStrAttribute)
            .exists { modId =>
              modId.name == "paranamer"
            }
        }
        // Get raw `File` objects.
        .map(_.data)
        .map(PluginCompat.toFile)
    },
    runChecks := jlinkBuildImage.value
  )
