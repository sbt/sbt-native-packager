.. _Custom:

Custom Package Formats
======================

This section provides an overview of different pacakging flavors.

SBT Assembly
------------

    **Main Goal**
    
    | Create a fat-jar with sbt-assembly in order to deliever a single,
    | self-containing jar as a package instead of the default lib/ structure

First add the sbt-assembly plugin to your `plugins.sbt` file.

.. code-block:: scala

    addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")
    
The next step is to remove all the jar mappings from the normal mappings and only add the
assembly jar. In this example we'll set the assembly jar name ourself, so we know exactly
what the output should look like. Finally we change the ``scriptClasspath`` so it only
contains the assembled jar. This is what the final ``build.sbt`` should contain:

.. code-block:: scala

    // the assembly settings
    assemblySettings

    // we specify the name for our fat jar
    jarName in assembly := "assembly-project.jar"

    // using the java server for this application. java_application is fine, too
    packageArchetype.java_server

    // removes all jar mappings in universal and appends the fat jar
    mappings in Universal := {
        // universalMappings: Seq[(File,String)]
        val universalMappings = (mappings in Universal).value 
        val fatJar = (assembly in Compile).value
        // removing means filtering
        val filtered = universalMappings filter { 
            case (file, name) =>  ! name.endsWith(".jar") 
        }
        // add the fat jar
        filtered :+ (fatJar -> ("lib/" + fatJar.getName))
    }
        

    // the bash scripts classpath only needs the fat jar
    scriptClasspath := Seq( (jarName in assembly).value )
    
Multi Module Builds
-------------------

    **Main Goal**
    
    | Aggregate multiple projects into one native package

If you want to aggregate different projects in a multi module build to a single package,
you can specify everthing in a single ``build.sbt``

.. code-block:: scala

    import NativePackagerKeys._

    name := "mukis-fullstack"

    // used like the groupId in maven
    organization in ThisBuild := "de.mukis"

    // all sub projects have the same version
    version in ThisBuild := "1.0"

    scalaVersion in ThisBuild := "2.11.2"

    // common dependencies
    libraryDependencies in ThisBuild ++= Seq(
        "com.typesafe" % "config" % "1.2.0"
    )

    // this is the root project, aggregating all sub projects
    lazy val root = Project(
        id = "root",
        base = file("."),
        // configure your native packaging settings here
        settings = packageArchetype.java_server++ Seq(
            maintainer := "John Smith <john.smith@example.com>",
            packageDescription := "Fullstack Application",
            packageSummary := "Fullstack Application",
            // entrypoint
            mainClass in Compile := Some("de.mukis.frontend.ProductionServer")
        ),
        // always run all commands on each sub project
        aggregate = Seq(frontend, backend, api)
    ) dependsOn(frontend, backend, api) // this does the actual aggregation

    // --------- Project Frontend ------------------
    lazy val frontend = Project(
        id = "frontend",
        base = file("frontend")
    ) dependsOn(api)


    // --------- Project Backend ----------------
    lazy val backend = Project(
        id = "backend",
        base = file("backend")
    ) dependsOn(api)

    // --------- Project API ------------------
    lazy val api = Project(
        id = "api",
        base = file("api")
    )
    
    
Custom Packaging Format
-----------------------

    **Main Goal**
    
    | Use native packager to define your own custom packaging format
    | and reuse stuff you already like

The very core principle of native packager are the ``mappings``. They are a sequence
of ``File -> String`` tuples, that map a file on your system to a location on your install
location.

Defining a custom mapping format is basically transforming these mappings into the format
of you choice. To do so, we recommend the following steps

1. Create a new configuration ``scope`` for you packaging type
2. Define a ``packageBin`` task in your new scope that transforms the mappings into a package

The following examples demonstrates how to create a simple *text format*, which lists all your
mappings inside a package format. A minimal ``build.sbt`` would look like this

.. code-block:: scala

    import NativePackagerKeys._

    val TxtFormat = config("txtFormat")

    val root = project.in(file("."))
        // adding your custom configuration scope
        .configs( TxtFormat )
        .settings(packageArchetype.java_server:_*)
        .settings(
            name := "mukis-custom-package",
            version := "1.0",
            mainClass in Compile := Some("de.mukis.ConfigApp"),
            maintainer in Linux := "Nepomuk Seiler <nepomuk.seiler@mukis.de>",
            packageSummary in Linux := "Custom application configuration",
            packageDescription := "Custom application configuration",
            // defining your custom configuration
            packageBin in TxtFormat := {
                val fileMappings = (mappings in Universal).value
                val output = target.value / s"${packageName.value}.txt"
                // create the is with the mappings. Note this is not the ISO format -.-
                IO.write(output, "# Filemappings\n")
                // append all mappings to the list
                fileMappings foreach {
                    case (file, name) => IO.append(output, s"${file.getAbsolutePath}\t$name${IO.Newline}")
                }
                output
            }
        )

To create your new "packageFormat" just run

.. code-block:: bash

    txtFormat:packageBin
    
If you want to read more about sbt configurations:

* `sbt tasks <http://www.scala-sbt.org/0.13/docs/Tasks.html>`_
* `sbt configurations <http://www.scala-sbt.org/0.13.5/docs/Detailed-Topics/Testing.html#additional-test-configurations-with-shared-sources>`_
* `custom configuration <http://stackoverflow.com/questions/18789477/define-custom-configuration-in-sbt>`_

