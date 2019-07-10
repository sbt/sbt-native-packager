package com.typesafe.sbt.packager.archetypes
package jlink

import scala.sys.process.{BasicIO, Process, ProcessBuilder}
import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtNativePackager.{Debian, Universal}
import com.typesafe.sbt.packager.Keys.{bundledJvmLocation, packageName}
import com.typesafe.sbt.packager.Compat._
import com.typesafe.sbt.packager.archetypes.jlink._
import com.typesafe.sbt.packager.archetypes.scripts.BashStartScriptKeys
import com.typesafe.sbt.packager.universal.UniversalPlugin

/**
  * == Jlink Application ==
  *
  * This class contains the default settings for creating and deploying an
  * application as a runtime image using the standard `jlink` utility.
  *
  * == Configuration ==
  *
  * This plugin adds new settings to configure your packaged application.
  *
  * @example Enable this plugin in your `build.sbt` with
  *
  * {{{
  *  enablePlugins(JlinkPlugin)
  * }}}
  */
object JlinkPlugin extends AutoPlugin {

  object autoImport extends JlinkKeys {
    val JlinkIgnore = JlinkPlugin.Ignore
  }

  import autoImport._

  override def requires = JavaAppPackaging

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    target in jlinkBuildImage := target.value / "jlink" / "output",
    jlinkBundledJvmLocation := "jre",
    bundledJvmLocation := Some(jlinkBundledJvmLocation.value),
    jlinkIgnoreMissingDependency :=
      (jlinkIgnoreMissingDependency ?? JlinkIgnore.nothing).value,
    // Don't use `fullClasspath in Compile` directly - this way we can inject
    // custom classpath elements for the scan.
    fullClasspath in jlinkBuildImage := (fullClasspath in Compile).value,
    jlinkModules := (jlinkModules ?? Nil).value,
    jlinkModules ++= {
      val log = streams.value.log
      val javaHome0 = javaHome.in(jlinkBuildImage).value.getOrElse(defaultJavaHome)
      val run = runJavaTool(javaHome0, log) _
      val paths = fullClasspath.in(jlinkBuildImage).value.map(_.data.getPath)
      val shouldIgnore = jlinkIgnoreMissingDependency.value

      // We can find the java toolchain version by parsing the `release` file. This
      // only works for Java 9+, but so does this whole plugin.
      // Alternatives:
      // - Parsing `java -version` output - the format is not standardized, so there
      // are a lot of weird incompatibilities.
      // - Parsing `java -XshowSettings:properties` output - the format is nicer,
      // but the command itself is subject to change without notice.
      val releaseFile = javaHome0 / "release"
      val javaVersion = IO
        .readLines(releaseFile)
        .collectFirst {
          case javaVersionPattern(feature) => feature
        }
        .getOrElse(sys.error("JAVA_VERSION not found in ${releaseFile.getAbsolutePath}"))

      // Jdeps has a few convenient options (like --print-module-deps), but those
      // are not flexible enough - we need to parse the full output.
      val jdepsOutput = runForOutput(run("jdeps", "--multi-release" +: javaVersion +: "-R" +: paths), log)

      val deps = jdepsOutput.linesIterator
      // There are headers in some of the lines - ignore those.
        .flatMap(PackageDependency.parse(_).iterator)
        .toSeq

      // Check that we don't have any dangling dependencies that were not
      // explicitly ignored.
      val missingDeps = deps
        .collect {
          case PackageDependency(dependent, dependee, PackageDependency.NotFound) =>
            (dependent, dependee)
        }
        .filterNot(shouldIgnore)
        .distinct
        .sorted

      if (missingDeps.nonEmpty) {
        log.error(
          "Dependee packages not found in classpath. You can use jlinkIgnoreMissingDependency to silence these."
        )
        missingDeps.foreach {
          case (a, b) =>
            log.error(s"  $a -> $b")
        }
        sys.error("Missing package dependencies")
      }

      // Collect all the found modules
      deps.collect {
        case PackageDependency(_, _, PackageDependency.Module(module)) =>
          module
      }.distinct
    },
    jlinkModulePath := (jlinkModulePath ?? Nil).value,
    jlinkModulePath ++= {
      fullClasspath.in(jlinkBuildImage).value.map(_.data)
    },
    jlinkOptions := (jlinkOptions ?? Nil).value,
    jlinkOptions ++= {
      val modules = jlinkModules.value

      if (modules.isEmpty) {
        sys.error("jlinkModules is empty")
      }

      JlinkOptions(
        addModules = modules,
        output = Some(target.in(jlinkBuildImage).value),
        modulePath = jlinkModulePath.value
      )
    },
    jlinkBuildImage := {
      val log = streams.value.log
      val javaHome0 = javaHome.in(jlinkBuildImage).value.getOrElse(defaultJavaHome)
      val run = runJavaTool(javaHome0, log) _
      val outDir = target.in(jlinkBuildImage).value

      IO.delete(outDir)

      runForOutput(run("jlink", jlinkOptions.value), log)

      outDir
    },
    mappings in jlinkBuildImage := {
      val prefix = jlinkBundledJvmLocation.value
      // make sure the prefix has a terminating slash
      val prefix0 = if (prefix.isEmpty) prefix else (prefix + "/")

      findFiles(jlinkBuildImage.value).map {
        case (file, string) => (file, prefix0 + string)
      }
    },
    mappings in Universal ++= mappings.in(jlinkBuildImage).value
  )

  // Extracts java version from a release file line (`JAVA_VERSION` property):
  // - if the feature version is 1, yield the minor version number (e.g. 1.9.0 -> 9);
  // - otherwise yield the major version number (e.g. 11.0.3 -> 11).
  private[jlink] val javaVersionPattern = """JAVA_VERSION="(?:1\.)?(\d+).*?"""".r

  // TODO: deduplicate with UniversalPlugin and DebianPlugin
  /** Finds all files in a directory. */
  private def findFiles(dir: File): Seq[(File, String)] =
    ((PathFinder(dir) ** AllPassFilter) --- dir)
      .pair(file => IO.relativize(dir, file))

  private lazy val defaultJavaHome: File =
    file(sys.props.getOrElse("java.home", sys.error("no java.home")))

  private def runJavaTool(jvm: File, log: Logger)(exeName: String, args: Seq[String]): ProcessBuilder = {
    val exe = (jvm / "bin" / exeName).getAbsolutePath

    log.info("Running: " + (exe +: args).mkString(" "))

    Process(exe, args)
  }

  // Like `ProcessBuilder.!!`, but this logs the output in case of a non-zero
  // exit code. We need this since some Java tools write their errors to stdout.
  // This uses `scala.sys.process.ProcessLogger` instead of the SBT `Logger`
  // to make it a drop-in replacement for `ProcessBuilder.!!`.
  private def runForOutput(builder: ProcessBuilder, log: scala.sys.process.ProcessLogger): String = {
    val buffer = new StringBuffer
    val code = builder.run(BasicIO(false, buffer, Some(log))).exitValue()

    if (code == 0) buffer.toString
    else {
      log.out(buffer.toString)
      scala.sys.error("Nonzero exit value: " + code)
    }
  }

  private object JlinkOptions {
    @deprecated("1.3.24", "")
    def apply(addModules: Seq[String] = Nil, output: Option[File] = None): Seq[String] =
      apply(addModules = addModules, output = output, modulePath = Nil)

    def apply(addModules: Seq[String], output: Option[File], modulePath: Seq[File]): Seq[String] =
      option("--output", output) ++
        list("--add-modules", addModules) ++
        list("--module-path", modulePath)

    private def option[A](arg: String, value: Option[A]): Seq[String] =
      value.toSeq.flatMap(a => Seq(arg, a.toString))

    private def list[A](arg: String, values: Seq[A]): Seq[String] =
      if (values.nonEmpty) Seq(arg, values.mkString(",")) else Nil
  }

  // Jdeps output row
  private final case class PackageDependency(dependent: String, dependee: String, source: PackageDependency.Source)

  private final object PackageDependency {
    sealed trait Source

    object Source {
      def parse(s: String): Source = s match {
        case "not found" => NotFound
        // We have no foolproof way to separate jars from modules here, so
        // we have to do something flaky.
        case name
            if name.toLowerCase.endsWith(".jar") ||
              !name.contains('.') ||
              name.contains(' ') =>
          JarOrDir(name)
        case name => Module(name)
      }
    }

    case object NotFound extends Source
    final case class Module(name: String) extends Source
    final case class JarOrDir(name: String) extends Source

    // Examples of package dependencies in jdeps output (whitespace may vary,
    // but there will always be some leading whitespace):
    // Dependency on a package(java.lang) in a module (java.base):
    //   foo.bar -> java.lang java.base
    // Dependency on a package (scala.collection) in a JAR
    // (scala-library-2.12.8.jar):
    //   foo.bar -> scala.collection scala-library-2.12.8.jar
    // Dependency on a package (foo.baz) in a class directory (classes):
    //   foo.bar -> foo.baz classes
    // Missing dependency on a package (qux.quux):
    //   foo.bar -> qux.quux not found
    // There are also jar/directory/module-level dependencies, but we are
    // not interested in those:
    // foo.jar -> scala-library-2.12.8.jar
    // classes -> java.base
    // foo.jar -> not found
    private val pattern = """^\s+([^\s]+)\s+->\s+([^\s]+)\s+([^\s].*?)\s*$""".r

    def parse(s: String): Option[PackageDependency] = s match {
      case pattern(dependent, dependee, source) =>
        Some(PackageDependency(dependent, dependee, Source.parse(source)))
      case _ => None
    }
  }

  object Ignore {
    val nothing: ((String, String)) => Boolean = Function.const(false)
    val everything: ((String, String)) => Boolean = Function.const(true)
    def only(dependencies: (String, String)*): ((String, String)) => Boolean = dependencies.toSet.contains

    /** This matches pairs by their respective ''package'' prefixes. This means that `"foo.bar"`
      * matches `"foo.bar"`, `"foo.bar.baz"`, but not `"foo.barqux"`. Empty
      * string matches anything.
      */
    def byPackagePrefix(prefixPairs: (String, String)*): ((String, String)) => Boolean = {
      case (a, b) =>
        prefixPairs.exists {
          case (prefixA, prefixB) =>
            packagePrefixMatches(prefixA, a) && packagePrefixMatches(prefixB, b)
        }
    }

    private def packagePrefixMatches(prefix: String, s: String): Boolean =
      prefix.isEmpty ||
        s == prefix ||
        s.startsWith(prefix + ".")
  }
}
