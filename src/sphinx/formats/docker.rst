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

You need the docker console client installed and version `1.3` or higher is required.
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

The Docker-spotify client is a provided dependency so you have to add it on your own.
It brings a lot of dependenciesthat could slow your build times. This is the reason
the dependency is marked as provided.

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
    The maintainer of the package, required by the Dockerfile format.

Environment Settings
~~~~~~~~~~~~~~~~~~~~

  ``dockerBaseImage``
    The image to use as a base for running the application. It should include binaries on the path for ``chown``, ``mkdir``, have a discoverable ``java`` binary, and include the user configured by ``daemonUser`` (``daemon``, by default).

  ``daemonUser in Docker``
    The user to use when executing the application. Files below the install path also have their ownership set to this user.

  ``dockerExposedPorts``
    A list of ports to expose from the Docker image.

  ``dockerExposedVolumes in Docker``
    A list of data volumes to make available in the Docker image.

  ``dockerEntrypoint in Docker``
    Overrides the default entrypoint for docker-specific service discovery tasks before running the application.
    Defaults to the bash executable script, available at ``bin/<script name>`` in the current ``WORKDIR`` of ``/opt/docker``.

Publishing Settings
~~~~~~~~~~~~~~~~~~~

  ``dockerRepository``
    The repository to which the image is pushed when the ``docker:publish`` task is run. This should be of the form ``[username]`` (assumes use of the ``index.docker.io`` repository) or ``[repository.host]/[username]``.

  ``dockerUpdateLatest``
    The flag to automatic update the latest tag when the ``docker:publish`` task is run. Default value is ``FALSE``.

  ``dockerAlias``
    The alias to be used for tagging the resulting image of the Docker build.
    The type of the setting key is ``DockerAlias`.
    Defaults to ``[dockerRepository/][name]:[version]``.

  ``dockerBuildOptions``
    Overrides the default Docker build options.
    Defaults to ``Seq("--force-rm", "-t", "[dockerAlias]")``. This default is expanded if ``dockerUpdateLatest`` is set to true.

  ``dockerBuildCommand``
    Overrides the default Docker build command.
    Defaults to ``Seq("docker", "build", "[dockerBuildOptions]", ".")``.

Tasks
-----
The Docker support provides the following commands:

  ``docker:stage``
    Generates a directory with the Dockerfile and environment prepared for creating a Docker image.

  ``docker:publishLocal``
    Builds an image using the local Docker server.

  ``docker:publish``
    Builds an image using the local Docker server, and pushes it to the configured remote repository.


Customize
---------

There are some predefined settings, which you can easily customize. These
settings are explained in some detail in the next sections. If you want to
describe your Dockerfile completely yourself, you can provide your own
`docker commands` as described in `Custom Dockerfile`_.

Docker Image Name
~~~~~~~~~~~~~~~~~

.. code-block:: scala

    packageName in Docker := packageName.value

    version in Docker := version.value

Docker Base Image
~~~~~~~~~~~~~~~~~

.. code-block:: scala

    dockerBaseImage := "dockerfile/java"

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
created (if they do not existend) and chowned.

Install Location
~~~~~~~~~~~~~~~~
The path to which the application is written can be changed with the setting.
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
    [info] List(Cmd(FROM,dockerfile/java:latest), Cmd(MAINTAINER,Your Name <y.n@yourcompany.com>), ...)



Remove Commands
~~~~~~~~~~~~~~~

SBT Native Packager added some commands you may not need. For example
the chowning of a exposed volume.

.. code-block:: scala

  import com.typesafe.sbt.packager.docker._

  // we want to filter the chown command for '/data'
  dockerExposedVolumes += "/data"

  dockerCommands := dockerCommands.value.filterNot {

    // ExecCmd is a case class, and args is a varargs variable, so you need to bind it with @
    case ExecCmd("RUN", args @ _*) => args.contains("chown") && args.contains("/data")

    // dont filter the rest
    case cmd                       => false
  }


Add Commands
~~~~~~~~~~~~

Adding commands is as straightforward as adding anything in a list.

.. code-block:: scala

  import com.typesafe.sbt.packager.docker._

  dockerCommands += Cmd("USER", daemonUser.value)

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
    Cmd("FROM", "dockerfile/java:latest"),
    Cmd("MAINTAINER", maintainer.value),
    ExecCmd("CMD", "echo", "Hello, World from Docker")
  )

Busybox/Ash Support
~~~~~~~~~~~~~~~~~~~

The default shell support for the Java archetype (JavaAppPackaging) is bash, with a Windows
bat file also generated.  Busybox is a popular minimal Docker base image that uses ash, a much
more limited shell than bash.  The result is that if you build a Docker image for Busybox the
generated bash launch script will likely not work.

Optionally you can use an ash-compatible archetype that derives from JavaAppPacking called
AshScriptPlugin.  Enable this by including:

.. code-block:: scala

  enablePlugins(AshScriptPlugin)

With this plugin enabled an ash-compatible launch script will be generated in your Docker image.

Just like for JavaAppPackaging you have the option of overriding the default script by supplying
your own src/templates/ash-template file.  When overriding the file don't forget to include
${{template_declares}} somewhere to populate $app_classpath $app_mainclass from your sbt project.
You'll likely need these to launch your program.
