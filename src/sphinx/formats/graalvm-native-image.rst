.. _graalvm-native-image-plugin:

GraalVM Native Image Plugin
===========================

GraalVM's ``native-image`` compiles Java programs AOT (ahead-of-time) into native binaries.

  https://www.graalvm.org/latest/reference-manual/native-image/ documents the AOT compilation of GraalVM.

The plugin supports both using a local installation of the GraalVM ``native-image`` utility, or building inside a
Docker container. If you intend to run the native image on Linux, then building inside a Docker container is
recommended since GraalVM native images can only be built for the platform they are built on. By building in a Docker
container, you can build Linux native images not only on Linux but also on Windows and macOS and for different architectures
like amd64 or arm64.

Requirements
------------

To build using a local installation of GraalVM, you must have the ``native-image`` utility of GraalVM in your ``PATH``.
To build using a docker container, you must have a working installation of docker.
To build for a different architecture, you must have docker with the buildx plugin and QEMU set up for the target architecture.

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

  sbt 'show GraalVMNativeImage/packageBin'


Required Settings
~~~~~~~~~~~~~~~~~

.. code-block:: scala

  enablePlugins(GraalVMNativeImagePlugin)


Settings
--------

``native-image`` Executable Command (Pay attention if you are using Windows OS)

  ``graalVMNativeImageCommand``
    Set this parameter to point to ``native-image`` or ``native-image.cmd``. Set this parameter if it is inconvenient to make ``native-image`` available in your ``PATH``.

    For example:

    .. code-block:: scala

      graalVMNativeImageCommand := "C:/Program Files/Java/graalvm/bin/native-image.cmd"

Docker Image Build Settings
~~~~~~~~~~~~~~~~~~~~~~~~~~~

By default, a local build will be done, expecting the ``native-image`` command to be on your ``PATH``. This can be
customized using the following settings.

  ``graalVMNativeImageGraalVersion``
    Setting this enables using a Docker container to build the native image.
    It should be in the format ``<packageName>:<tagName>``. `Supported packages <https://github.com/orgs/graalvm/packages?repo_name=container>`_ include:
    * ``graalvm-ce`` - Versions prior to and including 22.3.3. An intermediate image will be created.
    * ``native-image`` - Versions prior to and including 22.3.3. The docker image will be used directly.
    * ``graalvm-community`` - Versions after and including 17.0.7. An intermediate image will be created.
    * ``native-image-community`` - Versions after and including 17.0.7. The docker image will be used directly.

    The legacy format of specifying the version number is supported up to 22.3.3

    This setting has no effect if ``containerBuildImage`` is explicitly set.

    For example:

    .. code-block:: scala

      graalVMNativeImageGraalVersion := Some("19.1.1") // Legacy GraalVM versions supported up to 22.3.3
      graalVMNativeImageGraalVersion := Some("graalvm-ce:19.1.1") // Legacy GraalVM versions supported up to 22.3.3
      graalVMNativeImageGraalVersion := Some("native-image:19.1.1") // Uses the legacy native-image image from GraalVM directly
      graalVMNativeImageGraalVersion := Some("graalvm-community:17.0.8") // New GraalVM version scheme
      graalVMNativeImageGraalVersion := Some("native-image-community:17.0.8") // Uses the native-image image from GraalVM directly

  ``graalVMNativeImagePlatformArch``
    Setting this enables building the native image for a different platform architecture. Requires ``graalVMNativeImageGraalVersion``
    or ``containerBuildImage`` to be set. Multiplatform builds are currently not supported. Defaults to the platform of the host.
    If ``containerBuildImage`` is specified, ensure that your specified image has the same platform that you are targeting.

    Requires Docker buildx plugin with a valid builder and QEMU set up for the target platform architecture.
    `See here for more information <https://docs.docker.com/build/building/multi-platform/#building-multi-platform-images>`_.

    For example:

    .. code-block:: scala

      graalVMNativeImagePlatformArch := Some("arm64")
      graalVMNativeImagePlatformArch := Some("linux/amd64")

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

  ``GraalVMNativeImage / packageBin``
    Generates a native image using GraalVM.
