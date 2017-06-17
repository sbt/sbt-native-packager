package com.typesafe.sbt.packager.jdkpackager

import com.typesafe.sbt.packager.jdkpackager.JDKPackagerPlugin.autoImport._
import org.apache.tools.ant.{BuildEvent, BuildListener, ProjectHelper}
import sbt.Keys._
import sbt._

import scala.util.Try
import scala.xml.Elem

/**
  * Helpers for working with Ant build definitions
  *
  * @author <a href="mailto:fitch@datamininglab.com">Simeon H.K. Fitch</a>
  * @since 5/7/15
  */
object JDKPackagerAntHelper {

  /** Attempts to compute the path to the `javapackager` tool. */
  private[jdkpackager] def locateAntTasks(javaHome: Option[File], logger: Logger): Option[File] = {
    val jarname = "ant-javafx.jar"

    // This approach to getting JDK bits is borrowed from: http://stackoverflow.com/a/25163628/296509
    // Starting with an ordered list of possible java directory sources, create derivative and
    // then test for the tool. It's nasty looking because there's no canonical way of finding the
    // JDK from the JRE, and JDK_HOME isn't always defined.
    val searchPoints = Seq(
      // Build-defined
      javaHome,
      // Environment override
      sys.env.get("JDK_HOME").map(file),
      sys.env.get("JAVA_HOME").map(file),
      // MacOS X
      Try(sys.process.Process("/usr/libexec/java_home").!!.trim).toOption.map(file),
      // From system properties
      sys.props.get("java.home").map(file)
    )

    // Unlift searchPoint `Option`-s, and for each base directory, add the parent variant to cover nested JREs on Unix.
    val entryPoints =
      searchPoints.flatten.flatMap(f ⇒ Seq(f, f.getAbsoluteFile))

    // On Windows we're often running in the JRE and not the JDK. If JDK is installed,
    // it's likely to be in a parallel directory, with the "jre" prefix changed to "jdk"
    val entryPointsSpecialCaseWindows = entryPoints.flatMap { f ⇒
      if (f.getName.startsWith("jre"))
        Seq(f, f.getParentFile / ("jdk" + f.getName.drop(3)))
      else Seq(f)
    }

    // Now search for the tool
    entryPointsSpecialCaseWindows
      .map(_ / "lib" / jarname)
      .find { f ⇒
        logger.debug(s"Looking for '$jarname' in  '${f.getParent}'");
        f.exists()
      }
      .map { f ⇒
        logger.debug(s"Found '$f'!"); f
      }
  }

  type PlatformDOM = Elem

  /** Creates the `<fx:platform>` definition. */
  private[jdkpackager] def platformDOM(jvmArgs: Seq[String], properties: Map[String, String]): PlatformDOM =
    // format: OFF
    <fx:platform id="platform" javafx="8+" j2se="8+">
    {
      for {
        arg <- jvmArgs
      } yield <fx:jvmarg value={arg}/>
    }
    {
      for {
        (key, value) <- properties
      } yield <fx:property name={key} value={value}/>
    }
  </fx:platform>
  // format: ON

  type ApplicationDOM = Elem

  /** Create the `<fx:application>` definition. */
  private[jdkpackager] def applicationDOM(name: String,
                                          version: String,
                                          mainClass: Option[String],
                                          toolkit: JDKPackagerToolkit,
                                          appArgs: Seq[String]): ApplicationDOM =
    // format: OFF
    <fx:application id="app"
                    name={name}
                    version={version}
                    mainClass={mainClass.map(xml.Text.apply)}
                    toolkit={toolkit.arg}>
    {
      for {
        arg <- appArgs
      } yield <fx:argument>{arg}</fx:argument>
    }
    </fx:application>
  // format: ON

  type InfoDOM = Elem

  /** Create the `<fx:info>` definition. */
  private[jdkpackager] def infoDOM(name: String,
                                   description: String,
                                   maintainer: String,
                                   iconPath: Option[File],
                                   associations: Seq[FileAssociation]): InfoDOM =
    // format: OFF
    <fx:info id="info" title={name} description={description} vendor={maintainer}>
      {
        if (iconPath.nonEmpty) <fx:icon href={iconPath.get.getAbsolutePath} kind="default"/>
      }
      {
        for {
          fa <- associations
        } yield <fx:association extension={fa.extension} mimetype={fa.mimetype}
                                description={fa.description}
                                icon={fa.icon.map(_.getAbsolutePath).map(xml.Text.apply)}/>
      }
    </fx:info>
  // format: ON

  type DeployDOM = Elem

  /** Create the `<fx:deploy>` definition. */
  private[jdkpackager] def deployDOM(basename: String,
                                     packageType: String,
                                     mainJar: File,
                                     outputDir: File,
                                     infoDOM: InfoDOM): DeployDOM =
    // format: OFF
    <fx:deploy outdir={outputDir.getAbsolutePath}
               outfile={basename}
               nativeBundles={packageType}
               verbose="true">

      <fx:preferences install="true" menu="true" shortcut="true"/>


      <fx:application refid="app"/>

      <fx:platform refid="platform"/>

      {infoDOM}

      <fx:resources>
        <fx:fileset refid="jar.files"/>
        <fx:fileset refid="data.files"/>
      </fx:resources>

      <fx:bundleArgument arg="mainJar" value={"lib/" + mainJar.getName} />

    </fx:deploy>
  // format: ON

  type BuildDOM = xml.Elem

  /**
    * Create Ant project DOM for building packages, using ant-javafx.jar tasks.
    *
    * see: https://docs.oracle.com/javase/8/docs/technotes/guides/deploy/javafx_ant_task_reference.html
    */
  private[jdkpackager] def makeAntBuild(antTaskLib: Option[File],
                                        antExtraClasspath: Seq[File],
                                        name: String,
                                        sourceDir: File,
                                        mappings: Seq[(File, String)],
                                        platformDOM: PlatformDOM,
                                        applicationDOM: ApplicationDOM,
                                        deployDOM: DeployDOM): BuildDOM = {

    if (antTaskLib.isEmpty) {
      sys.error(
        "Please set key `antPackagerTasks in JDKPackager` to `ant-javafx.jar` path, " +
          "which should be find in the `lib` directory of the Oracle JDK 8 installation. For example (Windows):\n" +
          """(antPackagerTasks in JDKPackager) := Some("C:\\Program Files\\Java\\jdk1.8.0_45\\lib\\ant-javafx.jar")"""
      )
    }

    val taskClassPath = antTaskLib.get +: antExtraClasspath

    val (jarFiles, supportFiles) = mappings.partition(_._2.endsWith(".jar"))

    // format: OFF
    <project name={name} default="default" basedir="." xmlns:fx="javafx:com.sun.javafx.tools.ant">
      <target name="default">

        <property name="plugin.classpath" value={taskClassPath.mkString(":")}/>

        <taskdef resource="com/sun/javafx/tools/ant/antlib.xml"
                 uri="javafx:com.sun.javafx.tools.ant" classpath="${plugin.classpath}"/>

        {platformDOM}
        {applicationDOM}

        <fx:fileset id="jar.files" dir={sourceDir.getAbsolutePath} type="jar">
          {jarFiles.map(_._2).map(f => <include name={f}/> )}
        </fx:fileset>

        <fx:fileset id="data.files" dir={sourceDir.getAbsolutePath} type="data">
          {supportFiles.map(_._2).map(f => <include name={f}/> )}
        </fx:fileset>

        {deployDOM}

      </target>
    </project>
    // format: ON
  }

  /**
    * Locate the generated packge.
    * TODO: replace with something significantly more intelligent.
    * @param output output directory
    * @return generated file location
    */
  private[jdkpackager] def findResult(output: File, s: TaskStreams): Option[File] = {
    // Oooof. Need to do better than this to determine what was generated.
    val globs =
      Seq("*.dmg", "*.pkg", "*.app", "*.msi", "*.exe", "*.deb", "*.rpm")
    val finder = globs.foldLeft(PathFinder.empty)(_ +++ output ** _)
    val result = finder.getPaths.headOption
    result.foreach(f ⇒ s.log.info("Wrote " + f))
    result.map(file)
  }

  /** Serialize the Ant DOM to `build.xml`. */
  private[jdkpackager] def writeAntFile(outdir: File, dom: xml.Node, s: TaskStreams) = {
    if (!outdir.exists()) IO.createDirectory(outdir)
    val out = outdir / "build.xml"
    scala.xml.XML.save(out.getAbsolutePath, dom, "UTF-8", xmlDecl = true)
    s.log.info("Wrote " + out)
    out
  }

  /** Build package via Ant build.xml definition. */
  private[jdkpackager] def buildPackageWithAnt(buildXML: File, target: File, s: TaskStreams): File = {
    import org.apache.tools.ant.{Project ⇒ AntProject}

    val ap = new AntProject
    ap.setUserProperty("ant.file", buildXML.getAbsolutePath)
    val adapter = new AntLogAdapter(s)
    ap.addBuildListener(adapter)
    ap.init()

    val antHelper = ProjectHelper.getProjectHelper
    antHelper.parse(ap, buildXML)

    ap.executeTarget(ap.getDefaultTarget)
    ap.removeBuildListener(adapter)

    // Not sure what to do when we can't find the result
    findResult(target, s).getOrElse(target)
  }

  /** For piping Ant messages to sbt logger. */
  private class AntLogAdapter(s: TaskStreams) extends BuildListener {
    import org.apache.tools.ant.{Project ⇒ AntProject}
    def buildFinished(event: BuildEvent): Unit = ()
    def buildStarted(event: BuildEvent): Unit = ()
    def targetStarted(event: BuildEvent): Unit = ()
    def taskFinished(event: BuildEvent): Unit = ()
    def targetFinished(event: BuildEvent): Unit = ()
    def taskStarted(event: BuildEvent): Unit = ()
    def messageLogged(event: BuildEvent): Unit = event.getPriority match {
      case AntProject.MSG_ERR ⇒ s.log.error(event.getMessage)
      case AntProject.MSG_WARN ⇒ s.log.warn(event.getMessage)
      case AntProject.MSG_INFO ⇒ s.log.info(event.getMessage)
      case AntProject.MSG_VERBOSE ⇒ s.log.verbose(event.getMessage)
      case _ ⇒ s.log.debug(event.getMessage)
    }
  }
}
