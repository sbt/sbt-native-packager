.. _graalvm-native-image-plugin:

GraalVM Native Image Plugin
===========================

GraalVM's ``native-image`` compiles Java programs AOT (ahead-of-time) into native binaries.

  https://www.graalvm.org/docs/reference-manual/aot-compilation/ documents the AOT compilation of GraalVM.

The plugin supports both using a local installation of the GraalVM ``native-image`` utility, or building inside a
Docker container. If you intend to run the native image on Linux, then building inside a Docker container is
recommended since GraalVM native images can only be built for the platform they are built on. By building in a Docker
container, you can build Linux native images not just on Linux but also on Windows and macOS.

Requirements
------------

To build using a local installation of GraalVM, you must have the ``native-image`` utility of GraalVM in your ``PATH``.

``native-image`` quick installation
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To get started quickly, eg make ``native-image`` available in your ``PATH``,
you may reuse the script that is used for sbt-native-packager's continuous integration.
To do so, run the following. It will install GraalVM 1.0.0-rc8.

.. code-block:: bash

  source <(curl -o - https://raw.githubusercontent.com/sbt/sbt-native-packager/master/.travis/download-graalvm)

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

``native-image`` Executable Command (Pay attention if you are using Windows OS)

Putting ``native-image`` in ``PATH`` does not work for Windows. ``native-image`` is a batch file in Windows that calls another executable to compile the Java classes to a standalone executable. Therefore, the full path to the batch file e.g. ``C:\Program Files\Java\graalvm\bin\native-image.cmd`` must be provided. It is important to include ``.cmd``.

  ``graalVMNativeImageCommand``
    Set this parameter to point to ``native-image`` or ``native-image.cmd``. For Linux, set this parameter if it is inconvenient to make ``native-image`` available in your ``PATH``.

    For example:

    .. code-block:: scala

      graalVMNativeImageCommand := "C:/Program Files/Java/graalvm/bin/native-image.cmd"

Docker Image Build Settings
~~~~~~~~~~~~~~~~~~~~~~~~~~~

By default, a local build will be done, expecting the ``native-image`` command to be on your ``PATH``. This can be
customized using the following settings.

  ``graalVMNativeImageGraalVersion``
    Setting this enables generating a Docker container to build the native image, and then building it in that container.
    It must correspond to a valid version of the
    `Oracle GraalVM Community Edition Docker image <https://github.com/graalvm/container/pkgs/container/graalvm-ce/>`_. This setting has no
    effect if ``containerBuildImage`` is explicitly set.

    For example:

    .. code-block:: scala

      graalVMNativeImageGraalVersion := Some("19.1.1")

  ``containerBuildImage``

    Explicitly set a build image to use. The image must execute the Graal ``native-image`` command as its entry point.
    It can be configured like so:

    .. code-block:: scala

      containerBuildImage := Some("my-docker-username/graalvm-ce-native-image:19.1.1")

    A helper is provided to automatically generate a container build image from a base image that contains a Graal
    installation. For example, if you have a GraalVM enterprise edition docker image, you can turn it into a native
    image builder like so:

    .. code-block:: scala

      containerBuildImage := GraalVMNativeImagePlugin.generateContainerBuildImage("example.com/my-username/graalvm-ee:latest")

    The plugin will not build the native image container builder if it finds it in the local Docker registry already.
    The native image builders tag name can be seen in the logs if you wish to delete it to force a rebuild, in the above
    case, the name will be ``example.com-my-username-graalvm-ee:latest``.


Publishing Settings
~~~~~~~~~~~~~~~~~~~

  ``graalVMNativeImageOptions``
    Extra options that will be passed to the ``native-image`` command. By default, this includes the name of the main class.

GraalVM Resources
-----------------

If you are building the image in a docker container, and you have any resources that need to be available to the
``native-image`` command, such as files passed to ``-H:ResourceConfigurationFiles`` or
``-H:ReflectionConfigurationFiles``, you can place these in your projects ``src/graal`` directory. Any files in there
will be made available to the ``native-image`` docker container under the path ``/opt/graalvm/stage/resources``.

Tasks
-----
The GraalVM Native Image plugin provides the following commands:

  ``graalvm-native-image:packageBin``
    Generates a native image using GraalVM.
