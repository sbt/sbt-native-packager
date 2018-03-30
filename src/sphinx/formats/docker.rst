.. _docker-plugin:

Docker Plugin
=============

Docker images describe how to set up a container for running an application, including what files are present, and what program to run.

  https://docs.docker.com/introduction/understanding-docker/ provides an introduction to Docker.

  https://docs.docker.com/reference/builder/ describes the ``Dockerfile``: a file which describes how to set up the image.

sbt-native-packager focuses on creating a Docker image which can "just run" the application built by SBT.

.. note:: The docker plugin depends on the :ref:`universal-plugin`.

Requirements
------------

You need the version 1.10 or higher of the docker console client installed.
SBT Native Packager doesn't use the REST API, but instead uses the CLI directly.

It is currently not possible to provide authentication for Docker repositories from within the build.
The ``docker`` binary used by the build should already have been configured with the appropriate
authentication details. See https://docs.docker.com/reference/commandline/cli/#login.


Build
-----

.. code-block:: bash

  sbt docker:publishLocal


Required Settings
~~~~~~~~~~~~~~~~~

.. code-block:: scala

  enablePlugins(DockerPlugin)

Spotify java based docker client
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

You can also use the java-based spotify Docker client. Add this to your ``build.sbt``


.. code-block:: scala

  enablePlugins(DockerSpotifyClientPlugin)


and this to your ``plugins.sbt``

.. code-block:: scala

  libraryDependencies += "com.spotify" % "docker-client" % "3.5.13"

The Docker-spotify client is a provided dependency. You have to explicitly add it on your own. It brings a lot of dependencies
that could slow your build times. This is the reason the dependency is marked as provided.



Configuration
-------------

Settings and Tasks inherited from parent plugins can be scoped with ``Docker``.

.. code-block:: scala

  mappings in Docker := mappings.value


Settings
--------


Informational Settings
~~~~~~~~~~~~~~~~~~~~~~


  ``packageName in Docker``
    The name of the package for Docker (if different from general name).
    This will only affect the image name.

  ``version in Docker``
    The version of the package for Docker (if different from general version).  Often takes the form ``x.y.z``.

  ``maintainer in Docker``
    The maintainer of the package, recommended by the Dockerfile format.

Environment Settings
~~~~~~~~~~~~~~~~~~~~

  ``dockerBaseImage``
    The image to use as a base for running the application. It should include binaries on the path for ``chown``, ``mkdir``, have a discoverable ``java`` binary, and include the user configured by ``daemonUser`` (``daemon``, by default).

  ``daemonUser in Docker``
    The user to use when executing the application. Files below the install path also have their ownership set to this user.

  ``dockerExposedPorts``
    A list of TCP ports to expose from the Docker image.

  ``dockerExposedUdpPorts``
    A list of UDP ports to expose from the Docker image.

  ``dockerExposedVolumes``
    A list of data volumes to make available in the Docker image.

  ``dockerLabels``
    A map of labels that will be applied to the Docker image.

  ``dockerEntrypoint``
    Overrides the default entrypoint for docker-specific service discovery tasks before running the application.
    Defaults to the bash executable script, available at ``bin/<script name>`` in the current ``WORKDIR`` of ``/opt/docker``.

  ``dockerVersion``
    The docker server version. Used to leverage new docker features while maintaining backwards compatibility.

Publishing Settings
~~~~~~~~~~~~~~~~~~~

  ``dockerRepository``
    The repository to which the image is pushed when the ``docker:publish`` task is run. This should be of the form  ``[repository.host[:repository.port]]`` (assumes use of the ``index.docker.io`` repository) or ``[repository.host[:repository.port]][/username]`` (discouraged, but available for backwards compatibilty.).

  ``dockerUsername``
    The username or organization to which the image is pushed when the ``docker:publish`` task is run. This should be of the form ``[username]`` or ``[organization]``.

  ``dockerUpdateLatest``
    The flag to automatic update the latest tag when the ``docker:publish`` task is run. Default value is ``FALSE``.  In order to use this setting, the minimum docker console version required is 1.10. See https://github.com/sbt/sbt-native-packager/issues/871 for a detailed explanation.

  ``dockerAlias``
    The alias to be used for tagging the resulting image of the Docker build.
    The type of the setting key is ``DockerAlias``.
    Defaults to ``[dockerRepository/][dockerUsername/][packageName]:[version]``.

  ``dockerBuildOptions``
    Overrides the default Docker build options.
    Defaults to ``Seq("--force-rm", "-t", "[dockerAlias]")``. This default is expanded if ``dockerUpdateLatest`` is set to true.

  ``dockerExecCommand``
    Overrides the default Docker exec command.
    Defaults to ``Seq("docker")``

  ``dockerBuildCommand``
    Overrides the default Docker build command. The reason for this is that many systems restrict docker execution to root, and while the accepted guidance is to alias the docker command ``alias docker='/usr/bin/docker'``, neither Java nor Scala support passing aliases to sub-processes, and most build systems run builds using a non-login, non-interactive shell, which also have limited support for aliases, which means that the only viable option is to use ``sudo docker`` directly.
    Defaults to ``Seq("[dockerExecCommand]", "build", "[dockerBuildOptions]", ".")``.

  ``dockerRmiCommand``
    Overrides the default Docker rmi command. This may be used if force flags or other options need to be passed to the command ``docker rmi``.
    Defaults to ``Seq("[dockerExecCommand]", "rmi")`` and will be directly appended with the image name and tag.

Tasks
-----
The Docker plugin provides the following commands:

  ``docker:stage``
    Generates a directory with the Dockerfile and environment prepared for creating a Docker image.

  ``docker:publishLocal``
    Builds an image using the local Docker server.

  ``docker:publish``
    Builds an image using the local Docker server, and pushes it to the configured remote repository.

  ``docker:clean``
    Removes the built image from the local Docker server.


Customize
---------

There are some predefined settings which you can easily customize. These
settings are explained in some detail in the next sections. If you want to
describe your Dockerfile completely yourself, you can provide your own
`docker commands` as described in `Custom Dockerfile`_.

Docker Image Name and Version
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: scala

    packageName in Docker := packageName.value

    version in Docker := version.value

Docker Base Image
~~~~~~~~~~~~~~~~~

.. code-block:: scala

    dockerBaseImage := "openjdk"

Docker Repository
~~~~~~~~~~~~~~~~~

.. code-block:: scala

    dockerRepository := Some("dockeruser")

Docker Image Customization
~~~~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: scala

    dockerExposedPorts := Seq(9000, 9443)

    dockerExposedVolumes := Seq("/opt/docker/logs")


In order to work properly with `USER daemon` the exposed volumes are first
created (if they do not exist) and then chowned.

Install Location
~~~~~~~~~~~~~~~~
The path to which the application is written can be changed with the location setting.
The files from ``mappings in Docker`` are extracted underneath this directory.

.. code-block:: scala

  defaultLinuxInstallLocation in Docker := "/opt/docker"

Custom Dockerfile
~~~~~~~~~~~~~~~~~

All settings before are used to create a single sequence of docker commands.
You have the option to write all of them on your own, filter or change existing
commands or simply add some.

First of all you should take a look what you docker commands look like.
In your sbt console type

.. code-block:: bash

    > show dockerCommands
    [info] List(Cmd(FROM,openjdk:latest), Cmd(LABEL,MAINTAINER=Your Name <y.n@yourcompany.com>), ...)



Remove Commands
~~~~~~~~~~~~~~~

SBT Native Packager adds commands you may not need. For example,
the chowning of a exposed volume:

.. code-block:: scala

  import com.typesafe.sbt.packager.docker._

  // we want to filter the chown command for '/data'
  dockerExposedVolumes += "/data"

  // use filterNot to return all items that do NOT meet the criteria
  dockerCommands := dockerCommands.value.filterNot {

    // ExecCmd is a case class, and args is a varargs variable, so you need to bind it with @
    case ExecCmd("RUN", args @ _*) => args.contains("chown") && args.contains("/data")

    // don't filter the rest; don't filter out anything that doesn't match a pattern
    case cmd                       => false
  }


Add Commands
~~~~~~~~~~~~

Since ``dockerCommands`` is just a ``Sequence``, adding commands is straightforward:

.. code-block:: scala

  import com.typesafe.sbt.packager.docker._

  // use += to add an item to a Sequence
  dockerCommands += Cmd("USER", (daemonUser in Docker).value)

  // use ++= to merge a sequence with an existing sequence
  dockerCommands ++= Seq(
    // setting the run script executable
    ExecCmd("RUN",
      "chmod", "u+x",
       s"${(defaultLinuxInstallLocation in Docker).value}/bin/${executableScriptName.value}"),
    // setting a daemon user
    Cmd("USER", "daemon")
  )


Write from Scratch
~~~~~~~~~~~~~~~~~~

You can simply wipe out all docker commands with

.. code-block:: scala

  dockerCommands := Seq()


Now let's start adding some Docker commands.

.. code-block:: scala

  import com.typesafe.sbt.packager.docker._

  dockerCommands := Seq(
    Cmd("FROM", "openjdk:latest"),
    Cmd("LABEL", s"""MAINTAINER="${maintainer.value}""""),
    ExecCmd("CMD", "echo", "Hello, World from Docker")
  )

Busybox/Ash Support
~~~~~~~~~~~~~~~~~~~

Busybox is a popular minimal Docker base image that uses ash_, a much
more limited shell than bash.  By default, the Java archetype (:ref:`java-app-plugin`) generates two files for shell
support: a ``bash`` file, and a Windows ``.bat`` file.  If you build a Docker image for Busybox using the defaults, the
generated bash launch script will likely not work.

.. _ash: https://en.wikipedia.org/wiki/Almquist_shell

To handle this, you can use *AshScriptPlugin*, an ash-compatible archetype that is derived from the :ref:`java-app-plugin` archetype.
.  Enable this by including:

.. code-block:: scala

  enablePlugins(AshScriptPlugin)

With this plugin enabled an ash-compatible launch script will be generated in your Docker image.

Just like for :ref:`java-app-plugin`, you have the option of overriding the default script by supplying
your own ``src/templates/ash-template`` file.  When overriding the file don't forget to include
``${{template_declares}}`` somewhere to populate ``$app_classpath $app_mainclass`` from your sbt project.
You'll likely need these to launch your program.
