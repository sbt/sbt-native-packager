package com.typesafe.sbt
package packager
package archetypes

import Keys._
import sbt._
import sbt.Project.Initialize
import sbt.Keys.{ mappings, target, name, mainClass, normalizedName }
import linux.LinuxPackageMapping
import SbtNativePackager._

/**
 * This class contains the default settings for creating and deploying an archetypical Java application.
 *  A Java application archetype is defined as a project that has a main method and is run by placing
 *  all of its JAR files on the classpath and calling that main method.
 *
 *  This doesn't create the best of distributions, but it can simplify the distribution of code.
 *
 *  **NOTE:  EXPERIMENTAL**   This currently only supports universal distributions.
 */
object JavaAppPackaging {

  def settings: Seq[Setting[_]] = Seq(
    // Here we record the classpath as it's added to the mappings separately, so
    // we can use its order to generate the bash/bat scripts.
    scriptClasspathOrdering := Nil,
    // Note: This is sometimes on the classpath via dependencyClasspath in Runtime.
    // We need to figure out why sometimes the Attributed[File] is corrrectly configured
    // and sometimes not.
    scriptClasspathOrdering <+= (Keys.packageBin in Compile, Keys.projectID, Keys.artifact in Compile in Keys.packageBin) map { (jar, id, art) =>
      jar -> ("lib/" + makeJarName(id.organization, id.name, id.revision, art.name))
    },
    projectDependencyArtifacts <<= findProjectDependencyArtifacts,
    scriptClasspathOrdering <++= (Keys.dependencyClasspath in Runtime, projectDependencyArtifacts) map universalDepMappings,
    scriptClasspathOrdering <<= (scriptClasspathOrdering) map { _.distinct },
    mappings in Universal <++= scriptClasspathOrdering,
    scriptClasspath <<= scriptClasspathOrdering map makeRelativeClasspathNames,
    bashScriptExtraDefines := Nil,
    bashScriptDefines <<= (Keys.mainClass in Compile, scriptClasspath, bashScriptExtraDefines) map { (mainClass, cp, extras) =>
      val hasMain =
        for {
          cn <- mainClass
        } yield JavaAppBashScript.makeDefines(cn, appClasspath = cp, extras = extras)
      hasMain getOrElse Nil
    },
    makeBashScript <<= (bashScriptDefines, target in Universal, normalizedName) map makeUniversalBinScript,
    batScriptExtraDefines := Nil,
    batScriptReplacements <<= (normalizedName, Keys.mainClass in Compile, scriptClasspath, batScriptExtraDefines) map { (name, mainClass, cp, extras) =>
      mainClass map { mc =>
        JavaAppBatScript.makeReplacements(name = name, mainClass = mc, appClasspath = cp, extras = extras)
      } getOrElse Nil

    },
    makeBatScript <<= (batScriptReplacements, target in Universal, normalizedName) map makeUniversalBatScript,
    mappings in Universal <++= (makeBashScript, normalizedName) map { (script, name) =>
      for {
        s <- script.toSeq
      } yield s -> ("bin/" + name)
    },
    mappings in Universal <++= (makeBatScript, normalizedName) map { (script, name) =>
      for {
        s <- script.toSeq
      } yield s -> ("bin/" + name + ".bat")
    })

  def makeRelativeClasspathNames(mappings: Seq[(File, String)]): Seq[String] =
    for {
      (file, name) <- mappings
    } yield {
      // Here we want the name relative to the lib/ folder...
      // For now we just cheat...
      if (name startsWith "lib/") name drop 4
      else "../" + name
    }

  def makeUniversalBinScript(defines: Seq[String], tmpDir: File, name: String): Option[File] =
    if (defines.isEmpty) None
    else {
      val scriptBits = JavaAppBashScript.generateScript(defines)
      val script = tmpDir / "tmp" / "bin" / name
      IO.write(script, scriptBits)
      // TODO - Better control over this!
      script.setExecutable(true)
      Some(script)
    }

  def makeUniversalBatScript(replacements: Seq[(String, String)], tmpDir: File, name: String): Option[File] =
    if (replacements.isEmpty) None
    else {
      val scriptBits = JavaAppBatScript.generateScript(replacements)
      val script = tmpDir / "tmp" / "bin" / (name + ".bat")
      IO.write(script, scriptBits)
      Some(script)
    }

  // Constructs a jar name from components...(ModuleID/Artifact)
  def makeJarName(org: String, name: String, revision: String, artifactName: String): String =
    (org + "." +
      name + "-" +
      Option(artifactName.replace(name, "")).filterNot(_.isEmpty).map(_ + "-").getOrElse("") +
      revision + ".jar")

  // Determines a nicer filename for an attributed jar file, using the 
  // ivy metadata if available.
  def getJarFullFilename(dep: Attributed[File]): String = {
    val filename: Option[String] = for {
      module <- dep.metadata.get(AttributeKey[ModuleID]("module-id"))
      artifact <- dep.metadata.get(AttributeKey[Artifact]("artifact"))
    } yield makeJarName(module.organization, module.name, module.revision, artifact.name)
    filename.getOrElse(dep.data.getName)
  }

  // Here we grab the dependencies...
  def dependencyProjectRefs(build: sbt.BuildDependencies, thisProject: ProjectRef): Seq[ProjectRef] =
    build.classpathTransitive.get(thisProject).getOrElse(Nil)

  def filterArtifacts(artifacts: Seq[(Artifact, File)], config: Option[String]): Seq[(Artifact, File)] =
    for {
      (art, file) <- artifacts
      // TODO - Default to compile or default?
      if art.configurations.exists(_.name == config.getOrElse("default"))
    } yield art -> file

  def extractArtifacts(stateTask: Task[State], ref: ProjectRef): Task[Seq[Attributed[File]]] =
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
          sbt.Attributed.blank(file).
            put(sbt.Keys.moduleID.key, module).
            put(sbt.Keys.artifact.key, art)
        }
      }
    }

  // TODO - Should we pull in more than just JARs?  How do native packages come in?
  def isRuntimeArtifact(dep: Attributed[File]): Boolean =
    dep.get(sbt.Keys.artifact.key).map(_.`type` == "jar").getOrElse {
      val name = dep.data.getName
      !(name.endsWith(".jar") || name.endsWith("-sources.jar") || name.endsWith("-javadoc.jar"))
    }

  def findProjectDependencyArtifacts: Initialize[Task[Seq[Attributed[File]]]] =
    (sbt.Keys.buildDependencies, sbt.Keys.thisProjectRef, sbt.Keys.state) apply { (build, thisProject, stateTask) =>
      val refs = thisProject +: dependencyProjectRefs(build, thisProject)
      // Dynamic lookup of dependencies...
      val artTasks = (refs) map { ref => extractArtifacts(stateTask, ref) }
      val allArtifactsTask: Task[Seq[Attributed[File]]] =
        artTasks.fold[Task[Seq[Attributed[File]]]](task(Nil)) { (previous, next) =>
          for {
            p <- previous
            n <- next
          } yield (p ++ n.filter(isRuntimeArtifact))
        }
      allArtifactsTask
    }

  def findRealDep(dep: Attributed[File], projectArts: Seq[Attributed[File]]): Option[Attributed[File]] = {
    if (dep.data.isFile) Some(dep)
    else {
      projectArts.find { art =>
        // TODO - Why is the module not showing up for project deps?
        //(art.get(sbt.Keys.moduleID.key) ==  dep.get(sbt.Keys.moduleID.key)) &&
        ((art.get(sbt.Keys.artifact.key), dep.get(sbt.Keys.artifact.key))) match {
          case (Some(l), Some(r)) =>
            // TODO - extra attributes and stuff for comparison?
            // seems to break stuff if we do...
            (l.name == r.name)
          case _ => false
        }
      }
    }
  }

  // Converts a managed classpath into a set of lib mappings.
  def universalDepMappings(deps: Seq[Attributed[File]], projectArts: Seq[Attributed[File]]): Seq[(File, String)] =
    for {
      dep <- deps
      realDep <- findRealDep(dep, projectArts)
    } yield realDep.data -> ("lib/" + getJarFullFilename(realDep))
}