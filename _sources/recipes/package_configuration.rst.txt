.. _PackageConfigurations:

Build the same package with different configs
=============================================

If you want to build your application with different settings, e.g. for *test*, *staging* and *production*, then you
have three ways to do this.

.. tip:: All examples are shown in a simple ``build.sbt``. We recommend using AutoPlugins to encapsulate certain aspects
  of your build.

All examples can also be found in the `native-packager examples`_,

.. _native-packager examples: https://github.com/muuki88/sbt-native-packager-examples

SBT sub modules
---------------

The main idea is to create a submodule per configuration. We start with a simple project ``build.sbt``.


.. code-block :: scala

    name := "my-app"
    enablePlugins(JavaAppPackaging)

In the end we want to create three different packages (*test*, *stage*, *prod*) with the respective configurations.
We do this by creating an application module and three packaging submodules.

.. code-block :: scala

    // the application
    lazy val app = project
      .in(file("."))
      .settings(
	name := "my-app",
	libraryDependencies += "com.typesafe" % "config" % "1.3.0"
      )

Now that our application is defined in a module, we can add the three packaging submodules. We will override the ``resourceDirectory`` setting with our ``app`` resource directory to gain easy access to the applications resources.


.. code-block :: scala


    lazy val testPackage = project
      // we put the results  in a build folder
      .in(file("build/test"))
      .enablePlugins(JavaAppPackaging)
      .settings(
	// override the resource directory
	resourceDirectory in Compile := (resourceDirectory in (app, Compile)).value,
	mappings in Universal += {
	  ((resourceDirectory in Compile).value / "test.conf") -> "conf/application.conf"
	}
      )
      .dependsOn(app)

    // bascially identical despite the configuration differences
    lazy val stagePackage = project
      .in(file("build/stage"))
      .enablePlugins(JavaAppPackaging)
      .settings(
	resourceDirectory in Compile := (resourceDirectory in (app, Compile)).value,
	mappings in Universal += {
	  ((resourceDirectory in Compile).value / "stage.conf") -> "conf/application.conf"
	}
      )
      .dependsOn(app)

    lazy val prodPackage = project
      .in(file("build/prod"))
      .enablePlugins(JavaAppPackaging)
      .settings(
	resourceDirectory in Compile := (resourceDirectory in (app, Compile)).value,
	mappings in Universal += {
	  ((resourceDirectory in Compile).value / "prod.conf") -> "conf/application.conf"
	}
      )
      .dependsOn(app)

Now that you have your ``build.sbt`` set up, you can try building packages.

.. code-block :: bash

    # stages a test build in build/test/target/universal/stage
    testPackage/stage

    # creates a zip with the test configuration
    sbt testPackage/universal:packageBin


This technique is a bit verbose, but communicates very clear what is being built and why.

SBT parameters and Build Environment
------------------------------------

SBT is a java process, which means you can start it with system properties and use these in your build.
This pattern may be useful in other scopes as well. First we define an *AutoPlugin* that sets a build environment.

.. code-block :: scala

    import sbt._
    import sbt.Keys._
    import sbt.plugins.JvmPlugin

    /** sets the build environment */
    object BuildEnvPlugin extends AutoPlugin {

      // make sure it triggers automatically
      override def trigger = AllRequirements
      override def requires = JvmPlugin

      object autoImport {
	object BuildEnv extends Enumeration {
	  val Production, Stage, Test, Developement = Value
	}

	val buildEnv = settingKey[BuildEnv.Value]("the current build environment")
      }
      import autoImport._

      override def projectSettings: Seq[Setting[_]] = Seq(
	buildEnv := {
	  sys.props.get("env")
	     .orElse(sys.env.get("BUILD_ENV"))
	     .flatMap {
	       case "prod" => Some(BuildEnv.Production)
	       case "stage" => Some(BuildEnv.Stage)
	       case "test" => Some(BuildEnv.Test)
	       case "dev" => Some(BuildEnv.Developement)
	       case unkown => None
	     }
	     .getOrElse(BuildEnv.Developement)
	},
	// give feed back
	onLoadMessage := {
	  // depend on the old message as well
	  val defaultMessage = onLoadMessage.value
	  val env = buildEnv.value
	  s"""|$defaultMessage
	      |Running in build environment: $env""".stripMargin
	}
      )

    }


This plugin allows you to start sbt for example like

.. code-block :: bash

  sbt -Denv=prod
  [info] Set current project to my-app (in build file: ...)
  [info] Running in build environment: Production
  > show buildEnv
  [info] Production

Now we can use this ``buildEnv`` setting to change things. For example the ``mappings``. We recommend doing this in a
plugin as it involes quite some logic. In this case we decide which configuration file to map as ``application.conf``.

.. code-block :: scala

    mappings in Universal += {
      val confFile = buildEnv.value match {
	case BuildEnv.Developement => "dev.conf"
	case BuildEnv.Test => "test.conf"
	case BuildEnv.Stage => "stage.conf"
	case BuildEnv.Production => "prod.conf"
      }
      ((resourceDirectory in Compile).value / confFile) -> "conf/application.conf"
    }

Ofcourse you can change all other settings, package names, etc. as well. Building different output packages would look
like this

.. code-block :: bash

  sbt -Denv=test universal:packageBin
  sbt -Denv=stage universal:packageBin
  sbt -Denv=prod universal:packageBin


SBT configuration scope (not recommended)
-----------------------------------------

The other option is to generate additional scopes in order to build a package like ``prod:packageBin``. Scopes behave
counter intuitive sometimes, why we don't recommend this technique.

.. error:: This example is work in progress and doesn't work. Unless you are not very familiar with sbt we highly
  recommend using another technique.

A simple start may look like this

.. code-block :: scala

    lazy val Prod = config("prod") extend(Universal) describedAs("scope to build production packages")
    lazy val Stage = config("stage") extend(Universal) describedAs("scope to build staging packages")

    lazy val app = project
      .in(file("."))
      .enablePlugins(JavaAppPackaging)
      .configs(Prod, Stage)
      .settings(
	name := "my-app",
	libraryDependencies += "com.typesafe" % "config" % "1.3.0"
      )

You would expect ``prod:packageBin`` to work, but *extending* scopes doesn't imply inheriting tasks and settings. This
needs to be done manually. Append this to the ``app`` project.

.. code-block :: scala

    // inheriting tasks and settings
    .settings(inConfig(Prod)(UniversalPlugin.projectSettings))
    .settings(inConfig(Prod)(JavaAppPackaging.projectSettings))
    // define custom settings
    .settings(inConfig(Prod)(Seq(
      // you have to override everything carefully
      packageName := "my-prod-app",
      executableScriptName := "my-prod-app",
      // this is what we acutally want to change
      mappings += ((resourceDirectory in Compile).value / "prod.conf") -> "conf/application.conf"
    )))

Note that you have to know more on native-packager internals than you should, because you override all the necessary
settings with the intended values. Still this doesn't work as the universal plugin picks up the wrong mappings to build
the package.
