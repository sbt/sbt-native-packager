package com.typesafe.sbt.packager.archetypes
package jlink

import scala.sys.process.{Process, ProcessBuilder}
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
      val run = runJavaTool(javaHome.in(jlinkBuildImage).value, log) _
      val paths = fullClasspath.in(jlinkBuildImage).value.map(_.data.getPath)
      val shouldIgnore = jlinkIgnoreMissingDependency.value

      // Jdeps has a few convenient options (like --print-module-deps), but those
      // are not flexible enough - we need to parse the full output.
      val output = run("jdeps", "-R" +: paths) !! log

      val deps = output.linesIterator
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
    jlinkOptions := (jlinkOptions ?? Nil).value,
    jlinkOptions ++= {
      val modules = jlinkModules.value

      if (modules.isEmpty) {
        sys.error("jlinkModules is empty")
      }

      JlinkOptions(addModules = modules, output = Some(target.in(jlinkBuildImage).value))
    },
    jlinkBuildImage := {
      val log = streams.value.log
      val run = runJavaTool(javaHome.in(jlinkBuildImage).value, log) _
      val outDir = target.in(jlinkBuildImage).value

      IO.delete(outDir)

      run("jlink", jlinkOptions.value) !! log

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

  // TODO: deduplicate with UniversalPlugin and DebianPlugin
  /** Finds all files in a directory. */
  private def findFiles(dir: File): Seq[(File, String)] =
    ((PathFinder(dir) ** AllPassFilter) --- dir)
      .pair(file => IO.relativize(dir, file))

  private def runJavaTool(jvm: Option[File], log: Logger)(exeName: String, args: Seq[String]): ProcessBuilder = {
    val jh = jvm.getOrElse(file(sys.props.getOrElse("java.home", sys.error("no java.home"))))
    val exe = (jh / "bin" / exeName).getAbsolutePath

    log.info("Running: " + (exe +: args).mkString(" "))

    Process(exe, args)
  }

  private object JlinkOptions {
    def apply(addModules: Seq[String] = Nil, output: Option[File] = None): Seq[String] =
      option("--output", output) ++
        list("--add-modules", addModules)

    private def option[A](arg: String, value: Option[A]): Seq[String] =
      value.toSeq.flatMap(a => Seq(arg, a.toString))

    private def list(arg: String, values: Seq[String]): Seq[String] =
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
  }
}
