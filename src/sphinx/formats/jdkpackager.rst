.. _jdkpackager-plugin:

JDKPackager Plugin
==================

This plugin builds on Oracle's `javapackager`_ tool to generate  native application
launchers and installers for MacOS X, Windows, and Linux. This plugin takes the settings and staged output from
:ref:`java-app-plugin` and passes them through ``javapackager``
to create native formats per Oracle's provided features.

.. _javapackager: https://docs.oracle.com/javase/8/docs/technotes/guides/deploy/packager.html#CCHIHIIJ

The actual mechanism used by this plugin is the support provided by the ``lib/ant-javafx.jar`` Ant task library,
which provides more capabilities than the ``javapackager`` command line version, but the idea is the same.

This plugin's most relevant addition to the core `sbt-native-packager` capabilities is the generation of MacOS X App
bundles and associated ``.dmg`` and ``.pkg`` package formats.  With this plugin complete drag-and-drop installable
application bundles are possible, including the embedding of the JRE.  It can also generate Windows ``.exe`` and ``.msi``
installers provided the requisite tools are available on the Windows build platform (see below). While Linux package
formats are also possible via this plugin, it is likely the native `sbt-native-packager` support for ``.deb`` and
``.rpm`` formats will provide more configurability.

.. note:: The JDKPackagerPlugin depends on the :ref:`universal-plugin`, :ref:`java-app-plugin` and :ref:`Launcher Plugin <launcher-jar-plugin>`


Requirements
------------

The ``ant-javafx.jar`` library comes with *Oracle* JDK 8, found in the ``lib`` directory along with ``tools.jar`` and friends. If `sbt` is running under the JVM in Oracle JDK 8, then the plugin should be able to find the path to ``ant-javafx.jar``. If `sbt` is running under a different JVM, then the path to the tool will have to be specified via the ``jdkPackager:antPackagerTasks`` setting.

This plugin must be run on the platform of the target installer. The Oracle tooling does *not* provide a means of creating, say, Windows installers on MacOS, or MacOS on Linux, etc.

To use create Windows launchers & installers, the either the WIX Toolset (``msi``) or Inno Setup (``exe``) is required:

* `WIX Toolset <http://wixtoolset.org/>`_
* `Inno Setup <http://www.jrsoftware.org/isinfo.php>`_

For further details on the capabilities of `javapackager`, see the
`Windows <http://docs.oracle.com/javase/8/docs/technotes/tools/windows/javapackager.html>`_ and
`Unix <http://docs.oracle.com/javase/8/docs/technotes/tools/unix/javapackager.html>`_ references.
(Note: only a few of the possible settings are exposed through this plugin. Please submit a
`Github <https://github.com/sbt/sbt-native-packager/issues>`_ issue or pull request if something specific is desired.)


Enabling
--------

The plugin is enabled via the ``AutoPlugins`` facility:

.. code-block:: scala

  enablePlugins(JDKPackagerPlugin)

Build
-----

To use, first get your application working per ``JavaAppPackaging`` instructions (including the ``mainClass`` setting). Once that is working, run

.. code-block:: scala

  sbt jdkPackager:packageBin

By default, the plugin makes the installer type that is native to the current build platform in the directory
``target/jdkpackager/bundles``. The key ``jdkPackageType`` can be used to modify this behavior. Run
``help jdkPackageType`` in `sbt` for details. The most popular setting is likely to be ``jdkAppIcon``.

Settings
--------

*For the latest documentation reference the key descriptions in sbt.*

``jdkPackagerBasename``
  Filename sans extension for generated installer package.

``jdkPackagerType``
  Value passed as the `native` attribute to `fx:deploy` task.
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

.. note:: Because only a subset of the possible settings are exposed through he plugin, updates are likely required to fully
    make use of all formats. ``dmg`` currently the most tested type.


``jdkAppIcon``
  Path to platform-specific application icon:

  * `icns`: MacOS
  * `ico`: Windows
  * `png`: Linux

  Defaults a generically bland Java icon.

``jdkPackagerToolkit``
  GUI toolkit used in app. Either ``JavaFXToolkit`` (default) or ``SwingToolkit``

``jdkPackagerJVMArgs``
  Sequence of arguments to pass to the JVM.
  Default: ``Seq("-Xmx768m")``.
  `Oracle JVM argument docs <http://docs.oracle.com/javase/8/docs/technotes/guides/deploy/javafx_ant_task_reference.html#CIAHJIJG>`_

``jdkPackagerAppArgs``
  List of command line arguments to pass to the application on launch.
  Default: ``Seq.empty``
  `Oracle arguments docs <http://docs.oracle.com/javase/8/docs/technotes/guides/deploy/javafx_ant_task_reference.html#CACIJFHB>`_

``jdkPackagerProperties``
  Map of `System` properties to define in application.
  Default: ``Map.empty``
  `Oracle properties docs <http://docs.oracle.com/javase/8/docs/technotes/guides/deploy/javafx_ant_task_reference.html#CIAHCIFJ>`_

``jdkPackagerAssociations``
  Set of application file associations to register for the application.
  Example: `jdkPackagerAssociations := Seq(FileAssociation("foo", "application/x-foo", Foo Data File", iconPath))
  Default: `Seq.empty`
  Note: Requires JDK >= 8 build 40.
  `Oracle associations docs <http://docs.oracle.com/javase/8/docs/technotes/guides/deploy/javafx_ant_task_reference.html#CIAIDHBJ>`_

Example
-------

To take it for a test spin, run ``sbt jdkPackager:packageBin`` in the ``test-project-jdkpackager`` directory of the `sbt-native-packager` source. Then look in the ``target/jdkpackager/bundles`` directory for the result (specific name depends on platform built).

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

    maintainer := "Previously Owned Cats, Inc."

    packageSummary := "JDKPackagerPlugin example package thingy"

    packageDescription := "A test package using Oracle's JDK bundled javapackager tool."

    lazy val iconGlob = sys.props("os.name").toLowerCase match {
      case os if os.contains("mac") ⇒ "*.icns"
      case os if os.contains("win") ⇒ "*.ico"
      case _ ⇒ "*.png"
    }

    jdkAppIcon :=  (sourceDirectory.value ** iconGlob).getPaths.headOption.map(file)

    jdkPackagerType := "installer"

    jdkPackagerJVMArgs := Seq("-Xmx1g")

    jdkPackagerProperties := Map("app.name" -> name.value, "app.version" -> version.value)

    jdkPackagerAppArgs := Seq(maintainer.value, packageSummary.value, packageDescription.value)

    jdkPackagerAssociations := Seq(
        FileAssociation("foobar", "application/foobar", "Foobar file type"),
        FileAssociation("barbaz", "application/barbaz", "Barbaz file type", jdkAppIcon.value)
    )

    // Example of specifying a fallback location of `ant-javafx.jar` if plugin can't find it.
    (antPackagerTasks in JDKPackager) := (antPackagerTasks in JDKPackager).value orElse {
      for {
        f <- Some(file("/usr/lib/jvm/java-8-oracle/lib/ant-javafx.jar")) if f.exists()
      } yield f
    }


Debugging
---------

If you are having trouble figuring out how certain features affect the generated package, you can find the Ant-based build definition file in ``target/jdkpackager/build.xml``. You should be able to run Ant directly in that file assuming ``jdkPackager:packageBin`` has been run at least once.
