.. _graalvm-native-image-plugin:

GraalVM Native Image Plugin
=============

GraalVM's ``native-image`` compiles Java programs AOT (ahead-of-time) into native binaries.

  https://www.graalvm.org/docs/reference-manual/aot-compilation/ documents the AOT compilation of GraalVM.

Requirements
------------

You must have ``native-image`` of GraalVM in your ``PATH``.

Quick installation
~~~~~~~~~~~~~~~~~~

To get started quickly, eg make ``native-image`` available in your ``PATH``,
you may reuse the script that is used for sbt-native-packager's continuous integration.
To do so, run the following. It will install GraalVM 1.0.0-rc8.

.. code-block:: bash

  source <(curl -o - https://raw.githubusercontent.com/sbt/sbt-native-packager/6e1ee230350ce86c37b39c75f35718ac4a7f0a26/.travis/download-graalvm)

Build
-----

.. code-block:: bash

  sbt 'show graalvm-native-image:packageBin'


Required Settings
~~~~~~~~~~~~~~~~~

.. code-block:: scala

  enablePlugins(GraalVMNativeImagePlugin)


Settings
--------

Publishing Settings
~~~~~~~~~~~~~~~~~~~

  ``graalVMNativeImageOptions``
    Extra options that will be passed to the ``native-image`` command. By default, this includes the name of the main class.

Tasks
-----
The GraalVM Native Image plugin provides the following commands:

  ``graalvm-native-image:packageBin``
    Generates a native image using GraalVM.
