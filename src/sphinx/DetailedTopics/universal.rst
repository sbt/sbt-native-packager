.. _Universal:

Universal
=========

Universal packaging just takes a plain ``mappings`` configuration and generates various 
package files for distribution.  It allows you to provide your users a distribution
that is not tied to any particular platform, but may require manual labor to set up.


Getting Started with Universal Packaging
----------------------------------------
By default, all files found in the ``src/universal`` directory are included in the distribution.  So, the first step
in creating a a distribution is to place files in this directory in the layout you would like in the distributed zip file.

To add build generated files to the distribution, simple add a *mapping* to the ``mappings in Universal`` setting.  Let's
look at an example where we add the packaged jar of a project to the lib folder of a distribution:

.. code-block:: scala

    mappings in Universal <+= (packageBin in Compile) map { jar =>
      jar -> ("lib/" + jar.getName)
    }

The above does two things:  

1. It depends on ``packageBin in Compile`` which will generate a jar file form the project.
2. It creates a *mapping* (a ``Tuple2[File, String]``) which denotes the file and the location in the distribution as a string.

You can use this to add anything you desire to the package.


Universal Conventions
---------------------
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
the ``bin`` directory on Windows ``PATH`` environment variable (optionally disabled).  While these mappings provide a great start to nice packaging, it still
may be necessary to customize the native packaging for each platform.   This can be done by configuring those settings directly.

For example, even using generic mapping, debian has a requirement for changelog files to be fully formed.  Using the above generic mapping, we can configure just this
changelog in addition to the generic packaging by first defining a changelog in ``src/debian/changelog`` and then adding the following setting:


.. code-block:: scala

    linuxPackageMappings in Debian <+= (name in Universal, sourceDirectory in Debian) map { (name, dir) =>
      (packageMapping(
        (dir / "changelog") -> "/usr/share/doc/sbt/changelog.gz"
      ) withUser "root" withGroup "root" withPerms "0644" gzipped) asDocs()
    }

Notice how we're *only* modifying the package mappings for Debian linux packages.  For more information on the underlying packaging settings, see
:ref:`Windows` and :ref:`Linux` documentation.



Configurations
--------------
Universal packaging provides three Configurations:

  ``universal``
    For creating full distributions
  ``universal-docs``
    For creating bundles of documentation
  ``universal-src``
    For creating bundles of source.


Settings
--------
As we showed before, the Universal packages are completely configured through the use of the mappings key.  Simply
specify the desired mappings for a given configuration.  For Example:

.. code-block:: scala

    mappings in Universal <+= packageBin in Compile map { p => p -> "lib/foo.jar" }

However, sometimes it may be advantageous to customize the files for each archive separately.  For example, perhaps 
the .tar.gz has an additional README plaintext file in additon to a README.html.  To add this just to the .tar.gz file,
use the task-scope feature of sbt:

.. code-block:: scala

    mappings in Universal in package-zip-tarball += file("README") -> "README"
    
Besides ``mappings``, the ``name``, ``sourceDirectory`` and ``target`` configurations are all respected by universal packaging.

**Note: The Universal plugin will make anything in a bin/ directory executable.  This is to work around issues with JVM and file system manipulations.**

MappingsHelper
--------------

The `MappingsHelper`_ class provides a set of helper functions to make mapping directories easier.

.. code-block:: scala

    import NativePackagerHelper._
    
    mappings in Universal ++= directory("src/main/resources/cache")
    
    mappings in Universal ++= contentOf("src/main/resources/docs")
    
    mappings in Universal <++= sourceDirectory map (src => directory(src / "main" / "resources" / "cache"))
    
    mappings in Universal <++= sourceDirectory map (src => contentOf(src / "main" / "resources" / "docs"))


.. _MappingsHelper: https://github.com/sbt/sbt-native-packager/blob/master/src/main/scala/com/typesafe/sbt/packager/MappingsHelper.scala

Mapping Examples
----------------

SBT provides and IO and `Path`_ API, which
lets you define custom mappings easily. The files will appear in the generate universal zip, but also in your
debian/rpm/msi/dmg builds as described above in the conventions.

.. _Path: http://www.scala-sbt.org/0.13.1/docs/Detailed-Topics/Paths.html

The ``packageBin in Compile`` dependency is only needed, if your files get generated
during the ``packageBin`` command or before. For static files you can remove it.

Mapping a complete directory
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. code-block:: scala

    mappings in Universal <++= (packageBin in Compile, target ) map { (_, target) =>
        val dir = target / "scala-2.10" / "api"
        (dir.***) pair relativeTo(dir.getParentFile)
    } 

This maps the ``api`` folder directly to the generate universal zip. ``dir.***`` is a short way for
``dir ** "*"``, which means _select all files including *dir*. ``relativeTo(dir.getParentFile)``
generates a function with a ``file -> Option[String]`` mapping, which tries to generate a relative
string path from ``dir.getParentFile`` to the passed in file. ``pair`` uses the ``relativeTo``
function to generate a mapping ``File -> String``, which is *your file* to *relative destination*.

It exists some helper methods to map a complete directory in more human readable way.

.. code-block:: scala

    import NativePackagerHelper._

    //For dynamic content, e.g. something in the target directory which depends on a Task
    mappings in Universal <++= (packageBin in Compile, target) map { (_, target) =>
      directory(target / "scala-2.10" / "api")
    }

    //For static content it can be added to mappings directly
    mappings in Universal ++= directory("SomeResourcesToInclude")


Mapping the content of a directory
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. code-block:: scala

    mappings in Universal <++= (packageBin in Compile, target ) map { (_, target) =>
        val dir = target / "scala-2.10" / "api"
        (dir.*** --- dir) pair relativeTo(dir)
    }
    
The ``dir`` gets excluded and is used as root for ``relativeTo(dir)``.

Commands
--------

  ``universal:package-bin``
    Creates the ``zip`` universal package.
  
  ``universal:package-zip-tarball``
    Creates the ``tgz`` universal package.
    
  ``universal:package-xz-tarball``
    Creates the ``txz`` universal package.  The ``xz`` command can get better compression
    for some types of archives.

  ``universal:package-osx-dmg``
    Creates the ``dmg`` universal package.  This only work on OSX or systems with ``hdiutil``.
    
  ``universal-docs:package-bin``
    Creates the ``zip`` universal documentation package.
  
  ``universal-docs:package-zip-tarball``
    Creates the ``tgz`` universal documentation package.
    
  ``universal-docs:package-xz-tarball``
    Creates the ``txz`` universal documentation package.  The ``xz`` command can get better compression
    for some types of archives.
