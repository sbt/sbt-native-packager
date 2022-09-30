package com.typesafe.sbt.packager.archetypes
package jlink

import scala.collection.immutable
import scala.sys.process.{BasicIO, Process, ProcessBuilder}
import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtNativePackager.{Debian, Universal}
import com.typesafe.sbt.packager.Keys.{bundledJvmLocation, packageName}
import com.typesafe.sbt.packager.Compat._
import com.typesafe.sbt.packager.archetypes.jlink._
import com.typesafe.sbt.packager.archetypes.scripts.BashStartScriptKeys
import com.typesafe.sbt.packager.universal.UniversalPlugin
import java.io.File

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
    val JlinkIgnore: Ignore.type = JlinkPlugin.Ignore
  }

  import autoImport._

  override def requires: Plugins = JavaAppPackaging

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    jlinkBuildImage / target := target.value / "jlink" / "output",
    jlinkBundledJvmLocation := "jre",
    bundledJvmLocation := Some(jlinkBundledJvmLocation.value),
    jlinkIgnoreMissingDependency :=
      (jlinkIgnoreMissingDependency ?? JlinkIgnore.nothing).value,
    // Don't use `fullClasspath in Compile` directly - this way we can inject
    // custom classpath elements for the scan.
    jlinkBuildImage / fullClasspath := (Compile / fullClasspath).value,
    jlinkModules := (jlinkModules ?? Nil).value,
    jlinkModules ++= {
      val log = streams.value.log
      val javaHome0 = (jlinkBuildImage / javaHome).value.getOrElse(defaultJavaHome)
      val run = runJavaTool(javaHome0, log) _
      val paths = (jlinkBuildImage / fullClasspath).value.map(_.data.getPath)
      val modulePath = (jlinkModules / jlinkModulePath).value
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

      val modulePathOpts =
        if (modulePath.nonEmpty)
          Vector("--module-path", modulePath.mkString(File.pathSeparator))
        else Vector.empty

      // Jdeps has a few convenient options (like --print-module-deps), but those
      // are not flexible enough - we need to parse the full output.
      val jdepsOutput = run("jdeps", "--multi-release" +: javaVersion +: modulePathOpts ++: "-R" +: paths)

      val deps = parseJdeps(jdepsOutput)

      // Check that we don't have any dangling dependencies that were not
      // explicitly ignored.
      val missingDeps = deps
        .collect {
          case PackageDependency(dependent, dependee, PackageDependency.NotFound)
              if !shouldIgnore((dependent, dependee)) =>
            (dependent, dependee)
        }

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

      // Some JakartaEE artifacts use `java.*` module names, even though
      // they are not a part of the platform anymore.
      // https://github.com/eclipse-ee4j/ee4j/issues/34
      // This requires special handling on our part when deciding if the module
      // is a part of the platform or not.
      // At least the new modules shouldn't be doing this...
      val knownJakartaJavaModules = Set("java.annotation", "java.xml.bind", "java.xml.soap", "java.ws.rs")

      // Java platform modules that were officially removed.
      // https://www.oracle.com/java/technologies/javase/jdk-11-relnote.html#JDK-8190378
      val removedJavaModules = Set(
        "java.xml.ws",
        "java.xml.bind",
        "java.activation",
        "java.xml.ws.annotation",
        "java.corba",
        "java.transaction",
        "java.se.ee",
        "jdk.xml.ws",
        "jdk.xml.bind"
      )

      val filteredModuleDeps = deps
        .collect {
          case PackageDependency(_, _, PackageDependency.Module(m)) if m.startsWith("jdk.") || m.startsWith("java.") =>
            m
        }
        .diff(knownJakartaJavaModules)
        .diff(removedJavaModules)

      // We always want `java.base`, and `jlink` requires at least one module.
      (filteredModuleDeps + "java.base").toSeq
    },
    // No external modules by default: see #1247.
    jlinkModulePath := (jlinkModulePath ?? Nil).value,
    jlinkOptions := (jlinkOptions ?? Nil).value,
    jlinkOptions ++= {
      val modules = jlinkModules.value

      if (modules.isEmpty)
        sys.error("jlinkModules is empty")

      JlinkOptions(
        addModules = modules,
        output = Some((jlinkBuildImage / target).value),
        modulePath = (jlinkBuildImage / jlinkModulePath).value
      )
    },
    jlinkBuildImage := {
      val log = streams.value.log
      val javaHome0 = (jlinkBuildImage / javaHome).value.getOrElse(defaultJavaHome)
      val run = runJavaTool(javaHome0, log) _
      val outDir = (jlinkBuildImage / target).value

      IO.delete(outDir)

      run("jlink", jlinkOptions.value)

      outDir
    },
    jlinkBuildImage / mappings := {
      val prefix = jlinkBundledJvmLocation.value
      // make sure the prefix has a terminating slash
      val prefix0 = if (prefix.isEmpty) prefix else prefix + "/"

      findFiles(jlinkBuildImage.value).map {
        case (file, string) => (file, prefix0 + string)
      }
    },
    Universal / mappings ++= (jlinkBuildImage / mappings).value
  )

  // Extracts java version from a release file line (`JAVA_VERSION` property):
  // - if the feature version is 1, yield the minor version number (e.g. 1.9.0 -> 9);
  // - otherwise yield the major version number (e.g. 11.0.3 -> 11).
  private[jlink] val javaVersionPattern = """JAVA_VERSION="(?:1\.)?(\d+).*?"""".r

  private[jlink] def parseJdeps(jdepsOutput: String): immutable.TreeSet[PackageDependency] =
    jdepsOutput.linesIterator.foldLeft(
      immutable.TreeSet.empty[PackageDependency](PackageDependency.PackageDependencyOrdering)
    ) { (z, l) =>
      PackageDependency.parse(l) match {
        case Some(pd) => z + pd
        case _        => z
      }
    }

  // TODO: deduplicate with UniversalPlugin and DebianPlugin
  /** Finds all files in a directory. */
  private def findFiles(dir: File): Seq[(File, String)] =
    ((PathFinder(dir) ** AllPassFilter) --- dir)
      .pair(file => IO.relativize(dir, file))

  private lazy val defaultJavaHome: File =
    file(sys.props.getOrElse("java.home", sys.error("no java.home")))

  private def runJavaTool(jvm: File, log: Logger)(toolName: String, args: Seq[String]): String = {
    log.info("Running: " + (toolName +: args).mkString(" "))

    val toolLauncherClass = classOf[ru.eldis.toollauncher.ToolLauncher]
    val toolLauncherJar = file(
      // This assumes that the code source is a file or a directory (as opposed
      // to a remote URL) - but that should hold.
      toolLauncherClass.getProtectionDomain.getCodeSource.getLocation.getPath
    ).getAbsolutePath

    val javaExe = (jvm / "bin" / "java").getAbsolutePath

    IO.withTemporaryFile(s"snp-$toolName-", "args") { argFile =>
      IO.writeLines(argFile, args)

      val argFileArg = "@" + argFile.getAbsolutePath
      val builder = Process(Vector(javaExe, "-jar", toolLauncherJar, "-tool", toolName, argFileArg))

      runForOutput(builder, log)
    }
  }

  // Like `ProcessBuilder.!!`, but this logs the output in case of a non-zero
  // exit code. We need this since some Java tools write their errors to stdout.
  // This uses `scala.sys.process.ProcessLogger` instead of the SBT `Logger`
  // to make it a drop-in replacement for `ProcessBuilder.!!`.
  private def runForOutput(builder: ProcessBuilder, log: scala.sys.process.ProcessLogger): String = {
    val buffer = new StringBuffer
    val code = builder.run(BasicIO(withIn = false, buffer, Some(log))).exitValue()

    if (code == 0) buffer.toString
    else {
      log.err(buffer.toString)
      scala.sys.error("Nonzero exit value: " + code)
    }
  }

  private object JlinkOptions {
    def apply(addModules: Seq[String], output: Option[File], modulePath: Seq[File]): Seq[String] =
      option("--output", output) ++
        list("--add-modules", addModules, ",") ++
        list("--module-path", modulePath, File.pathSeparator)

    private def option[A](arg: String, value: Option[A]): Seq[String] =
      value.toSeq.flatMap(a => Seq(arg, a.toString))

    private def list[A](arg: String, values: Seq[A], separator: String): Seq[String] =
      if (values.nonEmpty) Seq(arg, values.mkString(separator)) else Nil
  }

  // Jdeps output row
  private[jlink] final case class PackageDependency(
    dependent: String,
    dependee: String,
    source: PackageDependency.Source
  )

  private[jlink] final object PackageDependency {

    implicit object PackageDependencyOrdering extends Ordering[PackageDependency] {
      override def compare(x: PackageDependency, y: PackageDependency): Int = {
        var result = x.dependent.compareTo(y.dependent)
        if (result == 0)
          result = x.dependee.compareTo(y.dependee)
        if (result == 0)
          result = SourceOrdering.compare(x.source, y.source)
        result
      }
    }

    implicit object SourceOrdering extends Ordering[Source] {
      override def compare(x: Source, y: Source): Int =
        (x, y) match {
          case (`x`, `x`)                    => 0
          case (NotFound, _) | (_, NotFound) => -1
          case (Classes, _) | (_, Classes)   => -1
          case (JarOrDir(n1), JarOrDir(n2))  => n1.compareTo(n2)
          case (_: JarOrDir, _)              => 1
          case (_, _: JarOrDir)              => 1
          case (Module(n1), Module(n2))      => n1.compareTo(n2)
        }
    }

    sealed trait Source

    object Source {
      def parse(s: String): Source =
        s match {
          case "not found"                          => NotFound
          case "classes"                            => Classes
          case "JDK internal API (jdk.unsupported)" => Module("jdk.unsupported")
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
    case object Classes extends Source
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
    // akka-actor-typed_2.13-2.6.15.jar -> java.base
    // classes -> java.base
    // foo.jar -> not found
    private val pattern = """^\s+([^\s]+)\s+->\s+([^\s]+)\s+([^\s].*?)\s*$""".r

    def parse(s: String): Option[PackageDependency] =
      s match {
        case pattern(dependent, dependee, source) if dependent != dependee =>
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
