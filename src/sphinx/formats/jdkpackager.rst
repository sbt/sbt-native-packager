JDKPackager Plugin
==================

JDK 8 from Oracle includes the tool `javapackager` (née `javafxpackager`), which generates native application launchers and installers for MacOS X, Windows, and Linux. This plugin complements the existing `sbt-native-packager` formats by taking the settings and staged output from `JavaAppPackaging` and passing them through `javapackager` to create native formats per Oracle's defined mechanisms.

This plugin's most relevant addition to the core `sbt-native-packager` capabilities is the generation of MacOS X App bundles, and associated `.dmg` and `.pkg` package formats. With this plugin complete drag-and-drop installable application bundles are possible, including the embedding of the JRE. It can also generate Windows `.exe` and `.msi` installers provided the requisite tools are available on the Windows build platform.

.. contents::
  :depth: 2

.. raw:: html

  <div class="alert alert-info" role="alert">
    <span class="glyphicon glyphicon-info-sign" aria-hidden="true"></span>
    The JDKPackagerPlugin depends on the Universal and JavaAppPackaging plugins. For inherited settings read the <a href="../archetypes/java_app/index.html">Java Applicaiton Plugin Documentation</a>
  </div>


Requirements
------------

The `javapackager` tool comes with JDK 8, and found in the `bin` directory along with `javac` and friends. (An earlier form of the tool was introduced in later forms of JDK 7 as `javafxpackager`.)  If `sbt` is running under the JVM in JDK 8, then the plugin should be able to find the path to `javapcakger`. If `sbt` is running under a different JVM, then the path to the tool will have to be specified via the ``jdkPackagerTool`` setting.

This plugin must be run on the platform of the target installer. The `javapackager` tool does not provide a means of creating, say, Windows installers on MacOS, etc.

To use create Windows launchers & installers, the either the WIX Toolset (``msi``) or Inno Setup (``exe``) is required:

* `WIX Toolset <http://wixtoolset.org/>`_
* `Inno Setup <http://www.jrsoftware.org/isinfo.php>`_

For further details on the capabilities of `javapackager`, see the `windows <http://docs.oracle.com/javase/8/docs/technotes/tools/windows/javapackager.html>`_ and `Unix <http://docs.oracle.com/javase/8/docs/technotes/tools/unix/javapackager.html>`_ references. (Note: only a few of the possible settings are exposed through this plugin. Please submit a `Github <https://github.com/sbt/sbt-native-packager/issues>`_ issue or pull request if something specific is desired.)


Enabling
--------

The plugin is enabled via the ``AutoPlugins`` facility:

.. code-block:: scala

  enablePlugins(JDKPackagerPlugin)

Build
-----

To use, first get your application working per `JavaAppPackaging` instructions (including the ``mainClass`` setting). Once that is working, run

.. code-block:: scala

  sbt jdkPackager:packageBin

By default, the plugin makes the installer type that is native to the current build platform in the directory `target/jdkpackager`. The key `jdkPackageType` can be used to modify this behavior. Run `help jdkPackageType` in sbt for details. The most popular setting is likely to be `jdkAppIcon`.

Settings
--------

``jdkPackagerTool``
  Path to `javapackager` or `javafxpackager` tool in JDK.

``jdkPackagerBasename``
  Filename sans extension for generated installer package. Defaults to ``packageName``.

``jdkPackagerType``
  Value passed as the `-native` argument to `javapackager -deploy` command.
  Per `javapackager` documentation, this may be one of the following:

  * ``all``: Runs all of the installers for the platform on which it is running, and creates a disk image for the application.
  * ``installer``: Runs all of the installers for the platform on which it is running.
  * ``image``: Creates a disk image for the application. On OS X, the image is the .app file. On Linux, the image is the directory that gets installed.
  * ``dmg``: Generates a DMG file for OS X.
  * ``pkg``: Generates a .pkg package for OS X.
  * ``mac.appStore``: Generates a package for the Mac App Store.
  * ``rpm``: Generates an RPM package for Linux.
  * ``deb``: Generates a Debian package for Linux.
  * ``exe``: Generates a Windows .exe package.
  * ``msi``: Generates a Windows Installer package.

.. raw:: html

  <div class="alert alert-info" role="alert">
    <span class="glyphicon glyphicon-info-sign" aria-hidden="true"></span>
    Because only a subset of the possible settings are exposed through he plugin, updates are likely required to fully make use of all formats. ``dmg`` currently the most tested type.
  </div>

``jdkAppIcon``
  Path to platform-specific application icon:

  * `icns`: MacOS
  * `ico`: Windows
  * `png`: Linux

  Defaults a generically bland Java icon.

JVM Options
-----------

Relevant JVM settings specified in the ``src/universal/conf/jvmopts`` file are processed and added to the `javapackager` call. See :doc:`Customize Java Applications</customizejavaapplications>` for details.


Example
-------

To take it for a test spin, run ``sbt jdkPackager:packageBin`` in the ``test-project-jdkpackager`` directory of the `sbt-native-packager` soruce. Then look in the ``target/jdkpackager/bundles`` directory for the result (specific name depends on platform built).

Here's what the build file looks like:

.. code-block:: scala

    name := "JDKPackagerPlugin Example"

    version := "0.1.0"

    organization := "com.foo.bar"

    libraryDependencies ++= Seq(
        "com.typesafe" % "config" % "1.2.1"
    )

    mainClass in Compile := Some("ExampleApp")

    enablePlugins(JDKPackagerPlugin)

    maintainer := "Simeon H.K Fitch <fitch@datamininglab.com>"

    packageSummary := "JDKPackagerPlugin example package thingy"

    packageDescription := "A test package using Oracle's JDK bundled javapackager tool."

    lazy val iconGlob = sys.props("os.name").toLowerCase match {
      case os if os.contains("mac") ⇒ "*.icns"
      case os if os.contains("win") ⇒ "*.ico"
      case _ ⇒ "*.png"
    }

    jdkAppIcon :=  (sourceDirectory.value ** iconGlob).getPaths.headOption.map(file)

    jdkPackagerType := "installer"





