package com.typesafe.sbt
package packager
package archetypes

import sbt._
import sbt.Keys.{javaOptions, mainClass, mappings, name, sourceDirectory, streams, target}
import packager.Keys.{executableScriptName, packageName}
import linux.{LinuxFileMetaData, LinuxPackageMapping}
import linux.LinuxPlugin.autoImport.{defaultLinuxInstallLocation, linuxPackageMappings}
import SbtNativePackager.{Debian, Universal}

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

  object autoImport extends JavaAppKeys with MaintainerScriptHelper

  import JavaAppPackaging.autoImport._

  override def requires =
    debian.DebianPlugin && rpm.RpmPlugin && windows.WindowsPlugin

  // format: off
  override def projectSettings = Seq(
    javaOptions in Universal := Nil,
    // Here we record the classpath as it's added to the mappings separately, so
    // we can use its order to generate the bash/bat scripts.
    scriptClasspathOrdering := Nil,
    // Note: This is sometimes on the classpath via dependencyClasspath in Runtime.
    // We need to figure out why sometimes the Attributed[File] is corrrectly configured
    // and sometimes not.
    scriptClasspathOrdering <+= (Keys.packageBin in Compile, Keys.projectID, Keys.artifact in Compile in Keys.packageBin) map { (jar, id, art) =>
      jar -> ("lib/" + makeJarName(id.organization, id.name, id.revision, art.name, art.classifier))
    },
    projectDependencyArtifacts <<= findProjectDependencyArtifacts,
    scriptClasspathOrdering <++= (Keys.dependencyClasspath in Runtime, projectDependencyArtifacts) map universalDepMappings,
    scriptClasspathOrdering <<= scriptClasspathOrdering map { _.distinct },
    mappings in Universal <++= scriptClasspathOrdering,
    scriptClasspath <<= scriptClasspathOrdering map makeRelativeClasspathNames,
    linuxPackageMappings in Debian <+= (packageName in Debian, defaultLinuxInstallLocation, target in Debian) map {
      (name, installLocation, target) =>
        // create empty var/log directory
        val d = target / installLocation
        d.mkdirs()
        LinuxPackageMapping(Seq(d -> (installLocation + "/" + name)), LinuxFileMetaData())
    }
  )
  // format: on

  private def makeRelativeClasspathNames(mappings: Seq[(File, String)]): Seq[String] =
    for {
      (file, name) <- mappings
    } yield {
      // Here we want the name relative to the lib/ folder...
      // For now we just cheat...
      if (name startsWith "lib/") name drop 4
      else "../" + name
    }

  /**
    * Constructs a jar name from components...(ModuleID/Artifact)
    */
  def makeJarName(org: String,
                  name: String,
                  revision: String,
                  artifactName: String,
                  artifactClassifier: Option[String]): String =
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
      module <- dep.metadata.get(AttributeKey[ModuleID]("module-id"))
      artifact <- dep.metadata.get(AttributeKey[Artifact]("artifact"))
    } yield makeJarName(module.organization, module.name, module.revision, artifact.name, artifact.classifier)
    filename.getOrElse(dep.data.getName)
  }

  // Here we grab the dependencies...
  private def dependencyProjectRefs(build: sbt.BuildDependencies, thisProject: ProjectRef): Seq[ProjectRef] =
    build.classpathTransitive.getOrElse(thisProject, Nil)

  private def filterArtifacts(artifacts: Seq[(Artifact, File)], config: Option[String]): Seq[(Artifact, File)] =
    for {
      (art, file) <- artifacts
      // TODO - Default to compile or default?
      if art.configurations.exists(_.name == config.getOrElse("default"))
    } yield art -> file

  private def extractArtifacts(stateTask: Task[State], ref: ProjectRef): Task[Seq[Attributed[File]]] =
    stateTask flatMap { state =>
      val extracted = Project extract state
      // TODO - Is this correct?
      val module = extracted.get(sbt.Keys.projectID in ref)
      val artifactTask = extracted get (sbt.Keys.packagedArtifacts in ref)
      for {
        arts <- artifactTask
      } yield {
        for {
          (art, file) <- arts.toSeq // TODO -Filter!
        } yield {
          sbt.Attributed.blank(file).put(sbt.Keys.moduleID.key, module).put(sbt.Keys.artifact.key, art)
        }
      }
    }

  // TODO - Should we pull in more than just JARs?  How do native packages come in?
  private def isRuntimeArtifact(dep: Attributed[File]): Boolean =
    dep.get(sbt.Keys.artifact.key).map(_.`type` == "jar").getOrElse {
      val name = dep.data.getName
      !(name.endsWith(".jar") || name.endsWith("-sources.jar") || name.endsWith("-javadoc.jar"))
    }

  private def findProjectDependencyArtifacts: Def.Initialize[Task[Seq[Attributed[File]]]] =
    (sbt.Keys.buildDependencies, sbt.Keys.thisProjectRef, sbt.Keys.state) apply { (build, thisProject, stateTask) =>
      val refs = thisProject +: dependencyProjectRefs(build, thisProject)
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

  private def findRealDep(dep: Attributed[File], projectArts: Seq[Attributed[File]]): Option[Attributed[File]] =
    if (dep.data.isFile) Some(dep)
    else {
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
    }

  // Converts a managed classpath into a set of lib mappings.
  private def universalDepMappings(deps: Seq[Attributed[File]],
                                   projectArts: Seq[Attributed[File]]): Seq[(File, String)] =
    for {
      dep <- deps
      realDep <- findRealDep(dep, projectArts)
    } yield realDep.data -> ("lib/" + getJarFullFilename(realDep))
}
