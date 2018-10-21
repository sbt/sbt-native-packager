.. _graalvm-native-image-plugin:

GraalVM Native Image Plugin
=============

GraalVM's ``native-image`` compiles Java programs AOT (ahead-of-time) into native binaries.

  https://www.graalvm.org/docs/reference-manual/aot-compilation/ documents the AOT compilation of GraalVM.

Requirements
------------

You must have ``native-image`` of GraalVM in your ``PATH``.

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
