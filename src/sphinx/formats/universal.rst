.. _universal-plugin:

Universal Plugin
================

The Universal Plugin creates a generic, or "universal" distribution package.  This is called "universal packaging."  Universal packaging just takes a plain ``mappings`` configuration and generates various
package files in the output format specified.  Because it creates a distribution
that is not tied to any particular platform it may require manual labor (more work from your users) to correctly install and set up.

Related Plugins
---------------

- :ref:`linux-plugin`
- :ref:`docker-plugin`
- :ref:`windows-plugin`


Requirements
------------

Depending on what output format you want to use, you need one of the following applications installed

* zip (if native)
* gzip
* xz
* tar
* hdiutil (for dmg)

Build
-----

There is a task for each output format

  **Zip**

  .. code-block:: bash

    sbt Universal/packageBin

  **Tar**

  .. code-block:: bash

    sbt Universal/packageZipTarball

  **Xz**

  .. code-block:: bash

    sbt Universal/packageXzTarball

  **Dmg**

  .. code-block:: bash

    sbt Universal/packageOsxDmg


Required Settings
~~~~~~~~~~~~~~~~~

The Universal Plugin has no mandatory fields.

Enable the universal plugin

.. code-block:: scala

  enablePlugins(UniversalPlugin)



Configurations
--------------

Settings and Tasks inherited from parent plugins can be scoped with ``Universal``.

Universal packaging provides three Configurations:

  ``Universal``
    For creating full distributions
  ``UniversalDocs``
    For creating bundles of documentation
  ``UniversalSrc``
    For creating bundles of source.


Here is how the values for ``name`` and ``packageName`` are used by the three configurations:

.. code-block:: scala

    Universal / name := name.value

    UniversalDocs / name := (Universal / name).value

    UniversalSrc / name := (Universal / name).value

    Universal / packageName := packageName.value

Settings
--------
As we showed before, the universal packages are completely configured through the use of mappings.  Simply
specify the desired mappings for a given configuration.  For example:

.. code-block:: scala

    Universal / mappings += (Compile / packageBin).value -> "lib/foo.jar"

However, sometimes it may be advantageous to customize the files for each archive separately.  For example, perhaps
the .tar.gz has an additional README plaintext file in addition to a README.html.  To add this just to the .tar.gz file,
use the task-scope feature of sbt:

.. code-block:: scala

    Universal / packageZipTarball / mappings += file("README") -> "README"

Besides ``mappings``, the ``name``, ``sourceDirectory`` and ``target`` configurations are all respected by universal packaging.

**Note: The Universal plugin will make anything in a bin/ directory executable.  This is to work around issues with JVM
and file system manipulations.**

Tasks
-----

  ``Universal / package-bin``
    Creates the ``zip`` universal package.

  ``Universal / package-zip-tarball``
    Creates the ``tgz`` universal package.

  ``Universal / package-xz-tarball``
    Creates the ``txz`` universal package.  The ``xz`` command can get better compression
    for some types of archives.

  ``Universal / package-osx-dmg``
    Creates the ``dmg`` universal package.  This only work on macOS or systems with ``hdiutil``.

  ``UniversalDocs / packageBin``
    Creates the ``zip`` universal documentation package.

  ``UniversalDocs / packageZipTarball``
    Creates the ``tgz`` universal documentation package.

  ``UniversalDocs / packageXzTarball``
    Creates the ``txz`` universal documentation package.  The ``xz`` command can get better compression
    for some types of archives.

Customize
---------

Universal Archive Options
~~~~~~~~~~~~~~~~~~~~~~~~~

You can customize the commandline options (if used) for the different zip formats.
If you want to force local for the `tgz` output add this line:

.. code-block:: scala

  Universal / packageZipTarball / universalArchiveOptions := Seq("--force-local", "-pcvf")

This will set the cli options for the `packageZipTarball` task in the `Universal` plugin to use the options ``--force-local`` and ``pcvf``.
Be aware that the above line will overwrite the default options.  You may want to prepend your options, doing something like:

.. code-block:: scala

  Universal / packageZipTarball / universalArchiveOptions :=
    (Seq("--exclude", "*~") ++ (Universal / packageZipTarball / universalArchiveOptions).value)

Currently, these task can be customized:

  ``Universal/package-zip-tarball``
    `Universal / packageZipTarball / universalArchiveOptions`

  ``Universal/package-xz-tarball``
    `Universal / packageXzTarball / universalArchiveOptions`

.. _universal-plugin-getting-started-with-packaging:

Getting Started with Universal Packaging
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
By default, all files found in the ``src/universal`` directory are included in the distribution.  So, the first step
in creating a distribution is to place files in this directory and organize them as you'd like in them to be in the distributed package.
If your output format is a zip file, for example, although the distribution will consist of just one zip file, the files and directories within that zip file will reflect the same organization and structure as ``src/universal``.

To add files generated by the build task to a distribution, simply add a *mapping* to the ``Universal / mappings`` setting.  Let's
look at an example where we add the packaged jar of a project to the lib folder of a distribution:

.. code-block:: scala

    Universal / mappings += {
      val jar = (Compile / packageBin).value
      jar -> ("lib/" + jar.getName)
    }

The above does two things:

1. It depends on ``Compile / packageBin`` which will generate a jar file form the project.
2. It creates a *mapping* (a ``Tuple2[File, String]``) which denotes the file and the location in the distribution as a string.

You can use this pattern to add anything you desire to the package.

**Note**

..

    If you are using an ``application archetype`` or the ``playframework``, the jar mapping is already defined and
    you should not include these in your ``build.sbt``. `issue 227`_

.. _issue 227: https://github.com/sbt/sbt-native-packager/issues/227


Universal Conventions
~~~~~~~~~~~~~~~~~~~~~
This plugin has a set of conventions for universal packages that enable the automatic generation of native packages.  The
universal convention has the following package layout:


.. code-block:: none

    bin/
       <scripts and things you want on the path>
    lib/
       <shared libraries>
    conf/
       <configuration files that should be accessible using platform standard config locations.>
    doc/
       <Documentation files that should be easily accessible. (index.html treated specially)>

If your plugin matches these conventions, you can enable the settings to automatically generate native layouts based on your universal package.  To do
so, add the following to your build.sbt:


.. code-block:: scala

    mapGenericFilesToLinux

    mapGenericFilesToWinows


In Linux, this mapping creates symlinks from platform locations to the install location of the universal package.  For example,
given the following packaging:


.. code-block:: none

    bin/
       cool-tool
    lib/
       cool-tool.jar
    conf/
       cool-tool.conf


The ``mapGenericFilesToLinux`` settings will create the following package (symlinks denoted with ``->``):


.. code-block:: none

    /usr/share/<pkg-name>/
       bin/
         cool-tool
       lib/
         cool-tool.jar
       conf/
         cool-tool.conf
    /usr/bin/
         cool-tool  -> /usr/share/<package-name>/bin/cool-tool
    /etc/<pkg-name> -> /usr/share/<package-name>/conf

The ``mapGenericFilesToWindows`` will construct an MSI that installs the application in ``<Platform Program Files>\<Package Name>`` and include
the ``bin`` directory on Windows ``PATH`` environment variable (optionally disabled).

While these mappings provide a great start to nice packaging, it still
may be necessary to customize the native packaging for each platform.   This can be done by configuring those settings directly.

For example, even using generic mapping, debian has a requirement for changelog files to be fully formed.  Using the above generic mapping, we can configure just this
changelog in addition to the generic packaging by first defining a changelog in ``src/debian/changelog`` and then adding the following setting:


.. code-block:: scala

    Debian / linuxPackageMappings +=
      (packageMapping(
        ((Debian / sourceDirectory).value / "changelog") -> "/usr/share/doc/sbt/changelog.gz"
      ) withUser "root" withGroup "root" withPerms "0644" gzipped) asDocs()

Notice how we're *only* modifying the package mappings for Debian linux packages.

For more information on the
underlying packaging settings, see :ref:`windows-plugin` and :ref:`linux-plugin` documentation.

Change/Remove Top Level Directory in Output
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Your output package (zip, tar, gz) by default contains a single folder
with your application. If you want to change this folder or remove this
top level directory completely use the `topLevelDirectory` setting.

Removing the top level directory

.. code-block:: scala

  topLevelDirectory := None


Changing it to another value, e.g. the packageName without the version

.. code-block:: scala

  topLevelDirectory := Some(packageName.value)

Or just a plain hardcoded string


.. code-block:: scala

  topLevelDirectory := Some("awesome-app")

Skip packageDoc task on stage
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The ``stage`` task forces a *javadoc.jar* build, which could slow down ``stage`` tasks performance. In order to deactivate
this behaviour, add this to your ``build.sbt``

.. code-block:: scala

    compile / packageDoc / mappings := Seq()

Source `issue 651`_.

.. _`issue 651`: https://github.com/sbt/sbt-native-packager/issues/651

MappingsHelper
~~~~~~~~~~~~~~

The `MappingsHelper`_ class provides a set of helper functions to make mapping directories easier.

  **sbt 0.13.5 and plugin 1.0.x or higher**

  .. code-block:: scala

      import NativePackagerHelper._

  **plugin  version 0.8.x or lower**

  .. code-block:: scala

    import com.typesafe.sbt.SbtNativePackager._
    import NativePackagerHelper._


You get a set of methods which will help you to create mappings very easily.

.. code-block:: scala

    Universal / mappings ++= directory("src/main/resources/cache")

    Universal / mappings ++= contentOf("src/main/resources/docs")
    
    Universal / mappings ++= directory(sourceDirectory.value / "main" / "resources" / "cache")

    Universal / mappings ++= contentOf(sourceDirectory.value / "main" / "resources" / "docs")


.. _MappingsHelper: http://www.scala-sbt.org/sbt-native-packager/latest/api/#com.typesafe.sbt.packager.MappingsHelper$

Mapping Examples
~~~~~~~~~~~~~~~~

SBT provides the `IO`_ and `Path`_ APIs, which
help make defining custom mappings easy. The files will appear in the generate universal zip, but also in your
debian/rpm/msi/dmg builds as described above in the conventions.

.. _IO: http://www.scala-sbt.org/0.13.1/docs/Detailed-Topics/Paths.html
.. _Path: http://www.scala-sbt.org/0.13.1/docs/Detailed-Topics/Paths.html

The ``Compile / packageBin`` dependency is only needed if your files get generated
during the ``packageBin`` command or before. For static files you can remove it.

Mapping a complete directory
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

There are some helper methods so you can create a mapping for a complete directory:

For static content, you can just add the directory to the mapping:

.. code-block:: scala

    Universal / mappings ++= directory("SomeDirectoryNameToInclude")

If you want to add everything in a directory where the path for the directory is dynamic, e.g. the ``scala-2.10/api`` directory that is nested under in the ``target`` directory, and ``target`` is defined in a task:

.. code-block:: scala

    Universal / mappings ~= (_ ++ directory(target.value / "scala-2.10" / "api"))



You can also use the following approach if, for example, you need more flexibility:

.. code-block:: scala

    Universal / mappings ++= {
        val dir = target.value / "scala-2.10" / "api"
        (dir ** AllPassFilter) pair relativeTo(dir.getParentFile)
    }

Here is what happens in this code:

    ``dir.***`` is a PathFinder_ method that creates a sequence of every file under a directory, *including the directory itself.*

    ``relativeTo()``  returns a String that is the path relative to whatever you pass to it.

    ``dir.getParentFile``  returns the parent of ``dir``.  In this example, it's the parent directory of whatever ``target`` is.

    ``pair`` is a PathFinder_ method that takes a function and applies it to every file (in the sequence), and returns a *(file, function-result)* tuple.

Putting it all together, this creates a map of every file under ``target/scala-2.10/api`` (including the directory ``target/scala-2.10/api`` itself)
with a string that is the path to the parent of ``target``.  This is a mapping for every file and a string that tells the universal packager where it is located.

For example:

if target = ``/Users/you/dev/fantasticApp/src/scala/fantasticApp-0.1-HOTFIX01``

and
``fantasticApp-0.1-HOTFIX01/scala-2.10/api/`` contains the files

.. code-block:: none

  somedata.csv
  README


Then the code above will produce this mapping:

.. code-block:: none

    ((/Users/you/dev/fantasticApp/src/scala/fantasticApp-0.1-HOTFIX01,fantasticApp-0.1-HOTFIX01),

    (/Users/you/dev/fantasticApp/src/scala/fantasticApp-0.1-HOTFIX01/README,fantasticApp-0.1-HOTFIX01/README),

    (//Users/you/dev/fantasticApp/src/scala/fantasticApp-0.1-HOTFIX01/somedata.csv,fantasticApp-0.1-HOTFIX01/somedata.csv))


Note that the first item of each pair is the full path to where the file exists on the system ``/Users/you.....``, and the
second part is the just the path starting after ``.../scala``.  That second part is what is returned from
``<each file>.relativeTo(dir.getParentFile)``.

.. _PathFinder: http://www.scala-sbt.org/0.13.1/docs/Detailed-Topics/Paths.html#path-finders

Mapping the content of a directory (excluding the directory itself)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. code-block:: scala

    Universal / mappings ++= {
        val dir = target.value / "scala-2.10" / "api"
        (dir ** AllPassFilter --- dir) pair relativeTo(dir)
    }

The ``dir`` gets excluded and is used as root for ``relativeTo(dir)``.

Filter/Remove mappings
^^^^^^^^^^^^^^^^^^^^^^

If you want to remove mappings, you have to filter_ the current list of mappings.
This example demonstrates how to build a fat jar with sbt-assembly, but using all
the convenience of the sbt native packager archetypes.

.. _filter: https://twitter.github.io/scala_school/collections.html#filter

tl;dr how to remove stuff

.. code-block:: scala

    // removes all jar mappings in universal and appends the fat jar
    Universal / mappings := {
        // universalMappings: Seq[(File,String)]
        val universalMappings = (Universal / mappings).value
        val fatJar = (Compile / assembly).value

        // removing means filtering
        // notice the "!" - it means NOT, so only keep those that do NOT have a name ending with "jar"
        val filtered = universalMappings filter {
            case (file, name) =>  ! name.endsWith(".jar")
        }

        // add the fat jar to our sequence of things that we've filtered
        filtered :+ (fatJar -> ("lib/" + fatJar.getName))
    }

The complete ``build.sbt`` should contain these settings if you want a single assembled fat jar.

.. code-block:: scala

    // the assembly settings
    assemblySettings

    // we specify the name for our fat jar
    assembly / jarName := "assembly-project.jar"

    // using the java server for this application. java_application would be fine, too
    packageArchetype.java_server

    // removes all jar mappings in universal and appends the fat jar
    Universal / mappings := {
        val universalMappings = (Universal / mappings).value
        val fatJar = (Compile / assembly).value
        val filtered = universalMappings filter {
            case (file, name) =>  ! name.endsWith(".jar")
        }
        filtered :+ (fatJar -> ("lib/" + fatJar.getName))
    }

    // the bash scripts classpath only needs the fat jar
    scriptClasspath := Seq( (assembly / jarName).value )
