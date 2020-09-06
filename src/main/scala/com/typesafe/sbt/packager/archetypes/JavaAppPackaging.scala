package com.typesafe.sbt.packager.archetypes

import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtNativePackager.{Debian, Universal}
import com.typesafe.sbt.packager._
import com.typesafe.sbt.packager.Keys.packageName
import com.typesafe.sbt.packager.linux.{LinuxFileMetaData, LinuxPackageMapping}
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.{defaultLinuxInstallLocation, linuxPackageMappings}
import com.typesafe.sbt.packager.Compat._

/**
  * == Java Application ==
  *
  * This class contains the default settings for creating and deploying an archetypical Java application.
  * A Java application archetype is defined as a project that has a main method and is run by placing
  * all of its JAR files on the classpath and calling that main method.
  *
  * == Configuration ==
  *
  * This plugin adds new settings to configure your packaged application.
  * The keys are defined in [[com.typesafe.sbt.packager.archetypes.JavaAppKeys]]
  *
  * @example Enable this plugin in your `build.sbt` with
  *
  * {{{
  *  enablePlugins(JavaAppPackaging)
  * }}}
  */
object JavaAppPackaging extends AutoPlugin {

  /**
    * Name of the bat template if user wants to provide custom one
    */
  val batTemplate = "bat-template"

  object autoImport extends JavaAppKeys with JavaAppKeys2 with MaintainerScriptHelper

  import JavaAppPackaging.autoImport._

  override def requires: Plugins =
    debian.DebianPlugin && rpm.RpmPlugin && docker.DockerPlugin && windows.WindowsPlugin

  override def projectSettings =
    Seq(
      javaOptions in Universal := Nil,
      // Here we record the classpath as it's added to the mappings separately, so
      // we can use its order to generate the bash/bat scripts.
      scriptClasspathOrdering := Nil,
      // Note: This is sometimes on the classpath via dependencyClasspath in Runtime.
      // We need to figure out why sometimes the Attributed[File] is correctly configured
      // and sometimes not.
      scriptClasspathOrdering += {
        val jar = (packageBin in Compile).value
        val id = projectID.value
        val art = (artifact in Compile in packageBin).value
        jar -> ("lib/" + makeJarName(id.organization, id.name, id.revision, art.name, art.classifier))
      },
      projectDependencyArtifacts := findProjectDependencyArtifacts.value,
      scriptClasspathOrdering ++= universalDepMappings(
        (dependencyClasspath in Runtime).value,
        projectDependencyArtifacts.value
      ),
      scriptClasspathOrdering := scriptClasspathOrdering.value.distinct,
      mappings in Universal ++= scriptClasspathOrdering.value,
      scriptClasspath := makeRelativeClasspathNames(scriptClasspathOrdering.value),
      linuxPackageMappings in Debian += {
        val name = (packageName in Debian).value
        val installLocation = defaultLinuxInstallLocation.value
        val targetDir = (target in Debian).value
        // create empty var/log directory
        val d = targetDir / installLocation
        d.mkdirs()
        LinuxPackageMapping(Seq(d -> (installLocation + "/" + name)), LinuxFileMetaData())
      },
      bundledJvmLocation := (bundledJvmLocation ?? None).value
    )

  private def makeRelativeClasspathNames(mappings: Seq[(File, String)]): Seq[String] =
    for {
      (_, name) <- mappings
    } yield
    // Here we want the name relative to the lib/ folder...
    // For now we just cheat...
    if (name startsWith "lib/") name drop 4
    else "../" + name

  /**
    * Constructs a jar name from components...(ModuleID/Artifact)
    */
  def makeJarName(
    org: String,
    name: String,
    revision: String,
    artifactName: String,
    artifactClassifier: Option[String]
  ): String =
    org + "." +
      name + "-" +
      Option(artifactName.replace(name, "")).filterNot(_.isEmpty).map(_ + "-").getOrElse("") +
      revision +
      artifactClassifier.filterNot(_.isEmpty).map("-" + _).getOrElse("") +
      ".jar"

  // Determines a nicer filename for an attributed jar file, using the
  // ivy metadata if available.
  private def getJarFullFilename(dep: Attributed[File]): String = {
    val filename: Option[String] = for {
      module <-
        dep.metadata
          // sbt 0.13.x key
          .get(AttributeKey[ModuleID]("module-id"))
          // sbt 1.x key
          .orElse(dep.metadata.get(AttributeKey[ModuleID]("moduleID")))
      artifact <- dep.metadata.get(AttributeKey[Artifact]("artifact"))
    } yield makeJarName(module.organization, module.name, module.revision, artifact.name, artifact.classifier)
    filename.getOrElse(dep.data.getName)
  }

  // Here we grab the dependencies...
  private def dependencyProjectRefs(build: BuildDependencies, thisProject: ProjectRef): Seq[ProjectRef] =
    build.classpathTransitive.getOrElse(thisProject, Nil)

  // TODO - Should we pull in more than just JARs?  How do native packages come in?
  private def isRuntimeArtifact(dep: Attributed[File]): Boolean =
    dep.get(sbt.Keys.artifact.key).map(_.`type` == "jar").getOrElse {
      val name = dep.data.getName
      !(name.endsWith(".jar") || name.endsWith("-sources.jar") || name.endsWith("-javadoc.jar"))
    }

  private def findProjectDependencyArtifacts: Def.Initialize[Task[Seq[Attributed[File]]]] =
    Def
      .task {
        val stateTask = state.taskValue
        val refs = thisProjectRef.value +: dependencyProjectRefs(buildDependencies.value, thisProjectRef.value)
        // Dynamic lookup of dependencies...
        val artTasks = refs map { ref =>
          extractArtifacts(stateTask, ref)
        }
        val allArtifactsTask: Task[Seq[Attributed[File]]] =
          artTasks.fold[Task[Seq[Attributed[File]]]](task(Nil)) { (previous, next) =>
            for {
              p <- previous
              n <- next
            } yield p ++ n.filter(isRuntimeArtifact)
          }
        allArtifactsTask
      }
      .flatMap(identity)

  private def extractArtifacts(stateTask: Task[State], ref: ProjectRef): Task[Seq[Attributed[File]]] =
    stateTask.flatMap { state =>
      val extracted = Project.extract(state)
      // TODO - Is this correct?
      val module = extracted.get(projectID in ref)
      val artifactTask = extracted.get(packagedArtifacts in ref)
      for {
        arts <- artifactTask
      } yield for {
        (art, file) <- arts.toSeq // TODO -Filter!
      } yield Attributed.blank(file).put(moduleID.key, module).put(artifact.key, art)
    }

  private def findRealDep(dep: Attributed[File], projectArts: Seq[Attributed[File]]): Option[Attributed[File]] =
    if (dep.data.isFile) Some(dep)
    else
      projectArts.find { art =>
        // TODO - Why is the module not showing up for project deps?
        //(art.get(sbt.Keys.moduleID.key) ==  dep.get(sbt.Keys.moduleID.key)) &&
        (art.get(sbt.Keys.artifact.key), dep.get(sbt.Keys.artifact.key)) match {
          case (Some(l), Some(r)) =>
            // TODO - extra attributes and stuff for comparison?
            // seems to break stuff if we do...
            l.name == r.name && l.classifier == r.classifier
          case _ => false
        }
      }

  // Converts a managed classpath into a set of lib mappings.
  private def universalDepMappings(
    deps: Seq[Attributed[File]],
    projectArts: Seq[Attributed[File]]
  ): Seq[(File, String)] =
    for {
      dep <- deps
      realDep <- findRealDep(dep, projectArts)
    } yield realDep.data -> ("lib/" + getJarFullFilename(realDep))
}
