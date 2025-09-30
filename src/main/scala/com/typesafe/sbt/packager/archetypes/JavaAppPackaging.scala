package com.typesafe.sbt.packager
package archetypes

import sbt.{*, given}
import sbt.Keys.*
import sbt.internal.BuildDependencies
import com.typesafe.sbt.SbtNativePackager.{Debian, Universal}
import com.typesafe.sbt.packager.Keys.packageName
import com.typesafe.sbt.packager.linux.{LinuxFileMetaData, LinuxPackageMapping}
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.{defaultLinuxInstallLocation, linuxPackageMappings}
import com.typesafe.sbt.packager.Compat.*
import xsbti.FileConverter

/**
  * ==Java Application==
  *
  * This class contains the default settings for creating and deploying an archetypical Java application. A Java
  * application archetype is defined as a project that has a main method and is run by placing all of its JAR files on
  * the classpath and calling that main method.
  *
  * ==Configuration==
  *
  * This plugin adds new settings to configure your packaged application. The keys are defined in
  * [[com.typesafe.sbt.packager.archetypes.JavaAppKeys]]
  *
  * @example
  *   Enable this plugin in your `build.sbt` with
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
      Universal / javaOptions := Nil,
      // Here we record the classpath as it's added to the mappings separately, so
      // we can use its order to generate the bash/bat scripts.
      scriptClasspathOrdering := Nil,
      // Note: This is sometimes on the classpath via Runtime / dependencyClasspath.
      // We need to figure out why sometimes the Attributed[File] is correctly configured
      // and sometimes not.
      scriptClasspathOrdering += {
        val jar = (Compile / packageBin).value
        val id = projectID.value
        val art = (Compile / packageBin / artifact).value
        jar -> ("lib/" + makeJarName(id.organization, id.name, id.revision, art.name, art.classifier))
      },
      projectDependencyArtifacts := findProjectDependencyArtifacts.value,
      scriptClasspathOrdering ++= universalDepMappings(
        (Runtime / dependencyClasspath).value,
        projectDependencyArtifacts.value,
        fileConverter.value
      ),
      scriptClasspathOrdering := scriptClasspathOrdering.value.distinct,
      Universal / mappings ++= scriptClasspathOrdering.value,
      scriptClasspath := makeRelativeClasspathNames(scriptClasspathOrdering.value),
      Debian / linuxPackageMappings += {
        val name = (Debian / packageName).value
        val installLocation = defaultLinuxInstallLocation.value
        val targetDir = (Debian / target).value
        // create empty var/log directory
        val d = targetDir / installLocation
        d.mkdirs()
        LinuxPackageMapping(Seq(d -> (installLocation + "/" + name)), LinuxFileMetaData())
      },
      bundledJvmLocation := (bundledJvmLocation ?? None).value
    )

  private def makeRelativeClasspathNames(mappings: Seq[(PluginCompat.FileRef, String)]): Seq[String] =
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
  private def getJarFullFilename(dep: Attributed[PluginCompat.FileRef]): String = {
    val filename: Option[String] = for {
      moduleStr <- dep.metadata.get(PluginCompat.moduleIDStr)
      artifactStr <- dep.metadata.get(PluginCompat.artifactStr)
      module = PluginCompat.parseModuleIDStrAttribute(moduleStr)
      artifact = PluginCompat.parseArtifactStrAttribute(artifactStr)
    } yield makeJarName(module.organization, module.name, module.revision, artifact.name, artifact.classifier)
    filename.getOrElse(PluginCompat.getName(dep.data))
  }

  // Here we grab the dependencies...
  private def dependencyProjectRefs(build: BuildDependencies, thisProject: ProjectRef): Seq[ProjectRef] =
    build.classpathTransitive.getOrElse(thisProject, Nil)

  // TODO - Should we pull in more than just JARs?  How do native packages come in?
  private def isRuntimeArtifact(dep: Attributed[PluginCompat.FileRef]): Boolean =
    dep
      .get(PluginCompat.artifactStr)
      .map(PluginCompat.parseArtifactStrAttribute)
      .map(_.`type` == "jar")
      .getOrElse {
        val name = PluginCompat.getName(dep.data)
        !(name.endsWith(".jar") || name.endsWith("-sources.jar") || name.endsWith("-javadoc.jar"))
      }

  private def findProjectDependencyArtifacts: Def.Initialize[Task[Seq[Attributed[PluginCompat.FileRef]]]] =
    Def
      .setting {
        val stateTask = state.taskValue
        val refs = thisProjectRef.value +: dependencyProjectRefs(buildDependencies.value, thisProjectRef.value)
        // Dynamic lookup of dependencies...
        val artTasks = refs map { ref =>
          extractArtifacts(stateTask, ref)
        }
        val allArtifactsTask: Task[Seq[Attributed[PluginCompat.FileRef]]] =
          artTasks.fold[Task[Seq[Attributed[PluginCompat.FileRef]]]](task(Nil)) { (previous, next) =>
            for {
              p <- previous
              n <- next
            } yield p ++ n.filter(isRuntimeArtifact)
          }
        allArtifactsTask
      }

  private def extractArtifacts(stateTask: Task[State], ref: ProjectRef): Task[Seq[Attributed[PluginCompat.FileRef]]] =
    stateTask.flatMap { state =>
      val extracted = Project.extract(state)
      // TODO - Is this correct?
      val module = extracted.get(ref / projectID)
      val artifactTask = extracted.get(ref / packagedArtifacts)
      for {
        arts <- artifactTask
      } yield for {
        (art, file) <- arts.toSeq // TODO -Filter!
      } yield Attributed
        .blank(file)
        .put(PluginCompat.moduleIDStr, PluginCompat.moduleIDToStr(module))
        .put(PluginCompat.artifactStr, PluginCompat.artifactToStr(art))
    }

  private def findRealDep(
    dep: Attributed[PluginCompat.FileRef],
    projectArts: Seq[Attributed[PluginCompat.FileRef]],
    conv0: FileConverter
  ): Option[Attributed[PluginCompat.FileRef]] = {
    implicit val conv: FileConverter = conv0
    if (PluginCompat.toFile(dep.data).isFile) Some(dep)
    else
      projectArts.find { art =>
        // TODO - Why is the module not showing up for project deps?
        // (art.get(sbt.Keys.moduleID.key) ==  dep.get(sbt.Keys.moduleID.key)) &&
        (art.get(PluginCompat.artifactStr), dep.get(PluginCompat.artifactStr)) match {
          case (Some(l0), Some(r0)) =>
            val l = PluginCompat.parseArtifactStrAttribute(l0)
            val r = PluginCompat.parseArtifactStrAttribute(r0)
            // TODO - extra attributes and stuff for comparison?
            // seems to break stuff if we do...
            l.name == r.name && l.classifier == r.classifier
          case _ => false
        }
      }
  }

  // Converts a managed classpath into a set of lib mappings.
  private def universalDepMappings(
    deps: Seq[Attributed[PluginCompat.FileRef]],
    projectArts: Seq[Attributed[PluginCompat.FileRef]],
    conv0: FileConverter
  ): Seq[(PluginCompat.FileRef, String)] =
    for {
      dep <- deps
      realDep <- findRealDep(dep, projectArts, conv0)
    } yield realDep.data -> ("lib/" + getJarFullFilename(realDep))
}
