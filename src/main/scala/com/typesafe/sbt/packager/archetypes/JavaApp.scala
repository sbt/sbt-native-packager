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
object JavaAppPackaging extends AutoPlugin with JavaAppStartScript {

  /**
    * Name of the bash template if user wants to provide custom one
    */
  val bashTemplate = "bash-template"

  /**
    * Name of the bat template if user wants to provide custom one
    */
  val batTemplate = "bat-template"

  /**
    * Location for the application.ini file used by the bash script to load initialization parameters for jvm and app
    */
  val appIniLocation = "${app_home}/../conf/application.ini"

  object autoImport extends JavaAppKeys with MaintainerScriptHelper

  import JavaAppPackaging.autoImport._

  override def requires =
    debian.DebianPlugin && rpm.RpmPlugin && docker.DockerPlugin && windows.WindowsPlugin

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
    scriptClasspathOrdering <<= (scriptClasspathOrdering) map { _.distinct },
    mappings in Universal <++= scriptClasspathOrdering,
    scriptClasspath <<= scriptClasspathOrdering map makeRelativeClasspathNames,
    bashScriptExtraDefines := Nil,
    // Create a bashConfigLocation if options are set in build.sbt
    bashScriptConfigLocation <<= bashScriptConfigLocation ?? Some(appIniLocation),
    bashScriptEnvConfigLocation <<= bashScriptEnvConfigLocation ?? None,
    mappings in Universal := {
      val log = streams.value.log
      val universalMappings = (mappings in Universal).value
      val dir = (target in Universal).value
      val options = (javaOptions in Universal).value

      bashScriptConfigLocation.value.collect {
        case location if options.nonEmpty =>
          val configFile = dir / "tmp" / "conf" / "application.ini"
          //Do not use writeLines here because of issue #637
          IO.write(configFile, ("# options from build" +: options).mkString("\n"))
          val filteredMappings = universalMappings.filter {
            case (file, path) => path != appIniLocation
          }
          // Warn the user if he tries to specify options
          if (filteredMappings.size < universalMappings.size) {
            log.warn("--------!!! JVM Options are defined twice !!!-----------")
            log.warn("application.ini is already present in output package. Will be overriden by 'javaOptions in Universal'")
          }
          (configFile -> cleanApplicationIniPath(location)) +: filteredMappings

      }.getOrElse(universalMappings)

    },

    // ---
    bashScriptDefines <<= (Keys.mainClass in (Compile, bashScriptDefines), scriptClasspath in bashScriptDefines, bashScriptExtraDefines, bashScriptConfigLocation) map { (mainClass, cp, extras, config) =>
      val hasMain =
        for {
          cn <- mainClass
        } yield JavaAppBashScript.makeDefines(cn, appClasspath = cp, extras = extras, configFile = config)
      hasMain getOrElse Nil
    },
    bashScriptTemplateLocation := (sourceDirectory.value / "templates" / bashTemplate),
    makeBashScript <<= (bashScriptTemplateLocation, bashScriptDefines, target in Universal, executableScriptName, sourceDirectory) map makeUniversalBinScript,
    batScriptExtraDefines := Nil,
    batScriptReplacements <<= (packageName, Keys.mainClass in (Compile, batScriptReplacements), scriptClasspath in batScriptReplacements, batScriptExtraDefines) map { (name, mainClass, cp, extras) =>
      mainClass map { mc =>
        JavaAppBatScript.makeReplacements(name = name, mainClass = mc, appClasspath = cp, extras = extras)
      } getOrElse Nil

    },
    batScriptTemplateLocation := (sourceDirectory.value / "templates" / batTemplate),
    makeBatScript <<= (batScriptTemplateLocation, batScriptReplacements, target in Universal, executableScriptName, sourceDirectory) map makeUniversalBatScript,
    mappings in Universal <++= (makeBashScript, executableScriptName) map { (script, name) =>
      for {
        s <- script.toSeq
      } yield s -> ("bin/" + name)
    },
    mappings in Universal <++= (makeBatScript, executableScriptName) map { (script, name) =>
      for {
        s <- script.toSeq
      } yield s -> ("bin/" + name + ".bat")
    },
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
    build.classpathTransitive.get(thisProject).getOrElse(Nil)

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
      val artTasks = (refs) map { ref =>
        extractArtifacts(stateTask, ref)
      }
      val allArtifactsTask: Task[Seq[Attributed[File]]] =
        artTasks.fold[Task[Seq[Attributed[File]]]](task(Nil)) { (previous, next) =>
          for {
            p <- previous
            n <- next
          } yield (p ++ n.filter(isRuntimeArtifact))
        }
      allArtifactsTask
    }

  private def findRealDep(dep: Attributed[File], projectArts: Seq[Attributed[File]]): Option[Attributed[File]] =
    if (dep.data.isFile) Some(dep)
    else {
      projectArts.find { art =>
        // TODO - Why is the module not showing up for project deps?
        //(art.get(sbt.Keys.moduleID.key) ==  dep.get(sbt.Keys.moduleID.key)) &&
        ((art.get(sbt.Keys.artifact.key), dep.get(sbt.Keys.artifact.key))) match {
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

  /**
    * Currently unused.
    * TODO figure out a proper way to ship default `application.ini` if necessary
    */
  protected def applicationIniTemplateSource: java.net.URL =
    getClass.getResource("application.ini-template")

  /**
    * @param path that could be relative to app_home
    * @return path relative to app_home
    */
  private def cleanApplicationIniPath(path: String): String =
    path.replaceFirst("\\$\\{app_home\\}/../", "")
}

/**
  * Mixin this trait to generate startup scripts provided in the classpath of native packager.
  *
  * @example A simple plugin definition could look like this
  *
  * {{{
  * object AkkaAppPackaging extends AutoPlugin with JavaAppStartScript {
  *   // templates have to be placed inside the com/typesafe/sbt.packager/archetypes/ resource folder
  *   // the name is also used to find user-defined scripts
  *   val bashTemplate = "your-bash-template"
  *   val batTemplate = "your-bat-template"
  *
  *   override def requires = JavaAppPackaging
  *
  *   override def projectSettings = settings
  *
  *   import JavaAppPackaging.autoImport._
  *
  *   private def settings: Seq[Setting[_]] = Seq(
  *     makeBashScript <<= (bashScriptDefines, target in Universal, executableScriptName, sourceDirectory) map makeUniversalBinScript(bashTemplate),
  *     makeBatScript <<= (batScriptReplacements, target in Universal, executableScriptName, sourceDirectory) map makeUniversalBatScript(batTemplate)
  *   )
  * }
  *
  * }}}
  */
trait JavaAppStartScript {

  def makeUniversalBinScript(defaultTemplateLocation: File,
                             defines: Seq[String],
                             tmpDir: File,
                             name: String,
                             sourceDir: File): Option[File] =
    if (defines.isEmpty) None
    else {
      val template = resolveTemplate(defaultTemplateLocation)
      val scriptBits = JavaAppBashScript.generateScript(defines, template)
      val script = tmpDir / "tmp" / "bin" / name
      IO.write(script, scriptBits)
      // TODO - Better control over this!
      script.setExecutable(true)
      Some(script)
    }

  def makeUniversalBinScript(
    bashTemplate: String
  )(defines: Seq[String], tmpDir: File, name: String, sourceDir: File): Option[File] =
    makeUniversalBinScript(sourceDir / "templates" / bashTemplate, defines, tmpDir, name, sourceDir)

  def makeUniversalBatScript(
    batTemplate: String
  )(replacements: Seq[(String, String)], tmpDir: File, name: String, sourceDir: File): Option[File] =
    makeUniversalBatScript(sourceDir / "templates" / batTemplate, replacements, tmpDir, name, sourceDir)

  def makeUniversalBatScript(defaultTemplateLocation: File,
                             replacements: Seq[(String, String)],
                             tmpDir: File,
                             name: String,
                             sourceDir: File): Option[File] =
    if (replacements.isEmpty) None
    else {
      val template = resolveTemplate(defaultTemplateLocation)
      val scriptBits = JavaAppBatScript.generateScript(replacements, template)
      val script = tmpDir / "tmp" / "bin" / (name + ".bat")
      IO.write(script, scriptBits)
      Some(script)
    }

  private def resolveTemplate(defaultTemplateLocation: File): URL =
    if (defaultTemplateLocation.exists)
      defaultTemplateLocation.toURI.toURL
    else
      getClass.getResource(defaultTemplateLocation.getName)

}
