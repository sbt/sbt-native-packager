.. _java-app-plugin:

Java Application Archetype
##########################

Application packaging focuses on how your application is launched (via a ``bash`` or ``bat`` script), how dependencies
are managed and how configuration and other auxiliary files are included in the final distributable. The
*JavaAppPackaging* archetype provides a default application structure and executable scripts to launch your application.

Additionally there is :ref:`java-server-plugin` which provides platform-specific functionality for installing your
application in server environments. You can customize specific debian and rpm packaging for a variety of platforms and
init service loaders including Upstart, System V and SystemD.

Features
========

The `JavaAppPackaging` archetype contains the following features.

* Default application mappings (no fat jar)
* Executable bash/bat script


Usage
=====

Enable the ``JavaAppPackaging`` plugin in your ``build.sbt`` with

.. code-block:: scala

  enablePlugins(JavaAppPackaging)

This archetype will use the ``mainClass`` setting of sbt (automatically discovers your main class) to generate
``bat`` and ``bin`` scripts for your project. In case you have multiple main classes you can point to a specific
class with the following setting:

.. code-block:: scala

   mainClass in Compile := Some("foo.bar.Main")

To create a staging version of your package call

.. code-block:: bash

  sbt stage

The universal layout produced in your ``target/universal/stage`` folder looks like the following:

.. code-block:: none

   bin/
     <app_name>       <- BASH script
     <app_name>.bat   <- cmd.exe script
   lib/
      <Your project and dependent jar files here.>


You can add additional files to the project by placing things in ``src/windows``, ``src/universal`` or ``src/linux`` as
needed. To see if your application runs:

.. code-block:: bash

  cd target/universal/stage
  ./bin/<app-name>

This plugin also enables all supported **packaging formats** as well. Currently **all formats** are supported by the
java app archetype! For example you can build *zips*, *deb* or *docker* by just enabling ``JavaAppPackaging``.

.. code-block:: bash

  sbt
  # create a zip file
  > universal:packageBin
  # create a deb file
  > debian:packageBin
  # publish a docker image to your local registry
  > docker:publishLocal



Settings & Tasks
================

This is a non extensive list of important settings and tasks this plugin provides. All settings
have sensible defaults.

  ``makeBashScript``
    Creates or discovers the bash script used by this project.

  ``makeBatScript``
    Creates or discovers the bat script used by this project.

  ``bashScriptTemplateLocation``
    The location of the bash script template.

  ``batScriptTemplateLocation``
    The location of the bat script template.

  ``bashScriptConfigLocation``
    The location of the bash script on the target system.
    **Default** ``${app_home}/../conf/application.ini``

  ``batScriptConfigLocation``
    The location of the bat script on the target system.
    **Default** ``%APP_HOME%\conf\application.ini``

  ``bashScriptExtraDefines``
    A list of extra definitions that should be written to the bash file template.

  ``batScriptExtraDefines``
    A list of extra definitions that should be written to the bat file template.


Start script options
====================

The start script provides a few standard options you can pass:

  ``-h | -help``
    Prints script usage

  ``-v | -verbose``
    Prints out more information

  ``-no-version-check``
    Don't run the java version check

  ``-jvm-debug <port>``
    Turn on JVM debugging, open at the given port

  ``-java-home <java home>``
    Override the default JVM home, it accept variable expansions, e.g.
    ``-java-home ${app_home}/../jre``

  ``-main``
    Define a custom main class


To configure the JVM these options are available

  ``JAVA_OPTS``
    environment variable, if unset uses "$java_opts"

  ``-Dkey=val``
    pass -Dkey=val directly to the java runtime

  ``-J-X``
    pass option -X directly to the java runtime (-J is stripped).
    E.g. ``-J-Xmx1024``

In order to pass **application arguments** you need to separate the jvm arguments from the
application arguments with ``--``. For example

.. code-block:: bash

    ./bin/my-app -Dconfig.resource=prod.conf -- -appParam1 -appParam2


Multiple Applications
=====================

If you have multiple main classes then the ``JavaAppPackaging`` archetype provides you with two different ways of
generating start scripts.

1. A start script for each entry point. This is the default behaviour, when no ``mainClass in Compile`` is set
2. One start script for the defined ``mainClass in Compile`` and forwarding scripts for all other main classes.

.. note:: What does *'forwarder script'* mean?

   Native-packager's start script provides a `-main` option to override the main class that should be executed.
   A *forwarder script* only overrides this attribute and forwards all other parameters to the normal start script.

   All customization you implemented for the main script will also apply for the forwarder scripts.

Multiple start scripts
----------------------

No configuration is needed. SBT sets ``mainClass in Compile`` automatically to ``None`` if multiple main classes are
discovered.

**Example:**

For two main classes ``com.example.FooMain`` and ``com.example.BarMain`` ``sbt stage`` will generate these scripts:

.. code-block:: none

   bin/
     bar-main
     bar-main.bat
     foo-main
     foo-main.bat


Single start script with forwarders
-----------------------------------

Generates a single start script for the defined main class in ``mainClass in Compile`` and forwarding scripts for all
other ``discoveredMainClasses in Compile``. The forwarder scripts call the defined start script and set the ``-main``
parameter to the concrete main class.

The start script name uses the ``executableScriptName`` setting for its name. The forwarder scripts use a simplified
version of the class name.


**Example:**

The ``build.sbt`` has an explicit main class set.

.. code-block:: scala

    name := "my-project"
    mainClass in Compile := Some("com.example.FooMain")

For two main classes ``com.example.FooMain`` and ``com.example.BarMain`` ``sbt stage`` will generate these scripts:

.. code-block:: none

   bin/
     bar-main
     bar-main.bat
     my-project
     my-project.bat


Now you can package your application as usual, but with multiple start scripts.

A note on script names
----------------------

When this plugin generates script names from main class names, it tries to generate readable and unique names:

1. An heuristic is used to split the fully qualified class names into words:

   .. code-block:: none

      pkg1.TestClass
      pkg2.AnUIMainClass
      pkg2.SomeXMLLoader
      pkg3.TestClass

   becomes

   .. code-block:: none

      pkg-1.test-class
      pkg-2.an-ui-main-class
      pkg-2.some-xml-loader
      pkg-3.test-class

2. Resulted lower-cased names are grouped by the simple class name.

    - Names from single-element groups are reduced to their lower-cased simple names.

    - Names that would otherwise collide by their simple names are used as is (that is, full names)
      with dots replaced by underscores

   So the final names will be:

   .. code-block:: none

      pkg-1_test-class
      an-ui-main-class
      some-xml-loader
      pkg-3_test-class

Please note that in some corner cases this may result in multiple scripts with the same name
in the resulting archive, but it is not expected to happen in normal circumstances.

Customize
=========

.. toctree::

   customize
