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

It is `currently not possible <https://github.com/sbt/sbt-native-packager/issues/654>`_ to provide authentication
for Docker repositories from within the build. The ``docker`` binary used by the build should already have been configured
with the appropriate authentication details. See https://docs.docker.com/engine/reference/commandline/login/ how to login
to a Docker registry with username and password.


Build
-----

.. code-block:: bash

  sbt Docker/publishLocal


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

  libraryDependencies += "com.spotify" % "docker-client" % "8.9.0"

The Docker-spotify client is a provided dependency. You have to explicitly add it on your own. It brings a lot of dependencies
that could slow your build times. This is the reason the dependency is marked as provided.



Configuration
-------------

Settings and Tasks inherited from parent plugins can be scoped with ``Docker``.

.. code-block:: scala

  Docker / mappings := mappings.value


Settings
--------


Informational Settings
~~~~~~~~~~~~~~~~~~~~~~


  ``Docker / packageName``
    The name of the package for Docker (if different from general name).
    This will only affect the image name.

  ``Docker / version``
    The version of the package for Docker (if different from general version).  Often takes the form ``x.y.z``.

  ``Docker / maintainer``
    The maintainer of the package, recommended by the Dockerfile format.

Environment Settings
~~~~~~~~~~~~~~~~~~~~

  ``dockerBaseImage``
    The image to use as a base for running the application. It should include binaries on the path for ``chown``, ``mkdir``, have a discoverable ``java`` binary, and include the user configured by ``daemonUser`` (``daemon``, by default).

  ``Docker / daemonUser``
    The user to use when executing the application. Files below the install path also have their ownership set to this user.

  ``dockerExposedPorts``
    A list of TCP ports to expose from the Docker image.

  ``dockerExposedUdpPorts``
    A list of UDP ports to expose from the Docker image.

  ``dockerExposedVolumes``
    A list of data volumes to make available in the Docker image.

  ``dockerLabels``
    A map of labels that will be applied to the Docker image.

  ``dockerEnvVars``
    A map of environment variables that will be applied to the Docker image.

  ``dockerEntrypoint``
    Overrides the default entrypoint for docker-specific service discovery tasks before running the application.
    Defaults to the bash executable script, available at ``bin/<script name>`` in the current ``WORKDIR`` of ``/opt/docker``.

  ``dockerPermissionStrategy``
    The strategy that decides how file permissions are set for the working directory inside the Docker image

    * ``DockerPermissionStrategy.MultiStage`` (default) uses multi-stage Docker build to call chmod ahead of time.
    * ``DockerPermissionStrategy.None`` does not attempt to change the file permissions, and use the host machine's file mode bits.
    * ``DockerPermissionStrategy.Run`` calls ``RUN`` in the Dockerfile. This has regression on the resulting Docker image file size.
    * ``DockerPermissionStrategy.CopyChown`` calls ``COPY --chown`` in the Dockerfile. Provided as a backward compatibility.

  ``dockerChmodType``
    The file permissions for the files copied into Docker image when ``MultiStage`` or ``Run`` strategy is used.

    * ``DockerChmodType.UserGroupReadExecute`` (default): chmod u=rX,g=rX
    * ``DockerChmodType.UserGroupRead``: chmod u=r,g=r
    * ``DockerChmodType.UserGroupWriteExecute``: chmod u=rwX,g=rwX
    * ``DockerChmodType.SyncGroupToUser``: chmod g=u
    * ``DockerChmodType.UserGroupPlusExecute``: chmod u+x,g+x (This is for ``dockerAdditionalPermissions``)
    * ``DockerChmodType.Custom``: Custom argument provided by the user.

  ``dockerAdditionalPermissions``
    Additional permissions typically used to give ``chmod +x`` rights for the executable files. By default generated Bash scripts are given ``DockerChmodType.UserGroupPlusExecute``.

  ``dockerVersion``
    The docker server version. Used to leverage new docker features while maintaining backwards compatibility.

  ``dockerApiVersion``
    The docker server API version. Used to leverage new docker features while maintaining backwards compatibility.

  ``dockerGroupLayers``
    The function mapping files into separate layers to increase docker cache hits.
    Lower index means the file would be a part of an earlier layer.
    The main idea behind this is to COPY dependencies *.jar's first as they should change rarely.
    In separate command COPY the application *.jar's that should change more often.
    Defaults to map the project artifacts and its dependencies to separate layers.
    To disable layers map all files to no layer using ``Docker / dockerGroupLayers := PartialFunction.empty``.

Publishing Settings
~~~~~~~~~~~~~~~~~~~

  ``dockerRepository``
    The repository to which the image is pushed when the ``Docker / publish`` task is run. This should be of the form  ``[repository.host[:repository.port]]`` (assumes use of the ``index.docker.io`` repository) or ``[repository.host[:repository.port]][/username]`` (discouraged, but available for backwards compatibilty.).

  ``dockerUsername``
    The username or organization to which the image is pushed when the ``Docker / publish`` task is run. This should be of the form ``[username]`` or ``[organization]``.

  ``dockerUpdateLatest``
    The flag to automatic update the latest tag when the ``Docker / publish`` task is run. Default value is ``FALSE``.  In order to use this setting, the minimum docker console version required is 1.10. See https://github.com/sbt/sbt-native-packager/issues/871 for a detailed explanation.

  ``dockerAlias``
    The alias to be used for tagging the resulting image of the Docker build.
    The type of the setting key is ``DockerAlias``.
    Defaults to ``[dockerRepository/][dockerUsername/][packageName]:[version]``.

  ``dockerAliases``
    The list of aliases to be used for tagging the resulting image of the Docker build.
    The type of the setting key is ``Seq[DockerAlias]``.
    Alias values are in format of ``[dockerRepository/][dockerUsername/][packageName]:[tag]`` where tags are list of including your project version and ``latest`` tag(if ``dockerUpdateLatest`` is enabled).
    To append additional aliases to this list, you can add them by extending ``dockerAlias``.
    ``dockerAliases ++= Seq(dockerAlias.value.withTag(Option("stable")), dockerAlias.value.withRegistryHost(Option("registry.internal.yourdomain.com")))``

  ``dockerBuildInit``
    Whether the ``--init`` build option should be passed to the Docker build. See :ref:`Init support` for when this may be useful.
    Defaults to ``false``.

  ``dockerBuildOptions``
    Overrides the default Docker build options.
    Defaults to ``Seq("--force-rm", "-t", "[dockerAlias]")``. This default is expanded if either ``dockerUpdateLatest`` or ``dockerBuildInit`` is set to true.

  ``dockerExecCommand``
    Overrides the default Docker exec command.
    Defaults to ``Seq("docker")``

  ``dockerBuildCommand``
    Overrides the default Docker build command. The reason for this is that many systems restrict docker execution to root, and while the accepted guidance is to alias the docker command ``alias docker='/usr/bin/docker'``, neither Java nor Scala support passing aliases to sub-processes, and most build systems run builds using a non-login, non-interactive shell, which also have limited support for aliases, which means that the only viable option is to use ``sudo docker`` directly.
    Defaults to ``Seq("[dockerExecCommand]", "build", "[dockerBuildOptions]", ".")``.

  ``dockerRmiCommand``
    Overrides the default Docker rmi command. This may be used if force flags or other options need to be passed to the command ``docker rmi``.
    Defaults to ``Seq("[dockerExecCommand]", "rmi")`` and will be directly appended with the image name and tag.

  ``dockerAutoremoveMultiStageIntermediateImages``
    If intermediate images should be automatically removed when ``MultiStage`` strategy is used.
    Intermediate images usually aren't needed after packaging is finished and therefore defaults to ``true``.
    All intermediate images are labeled ``snp-multi-stage=intermediate``.
    If set to ``false`` and you want to remove all intermediate images at a later point, you can therefore do that by filtering for this label:
    ``docker image prune -f --filter label=snp-multi-stage=intermediate``

Tasks
-----
The Docker plugin provides the following commands:

  ``Docker / stage``
    Generates a directory with the Dockerfile and environment prepared for creating a Docker image.

  ``Docker / publishLocal``
    Builds an image using the local Docker server.

  ``Docker / publish``
    Builds an image using the local Docker server, and pushes it to the configured remote repository.

  ``Docker / clean``
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

    Docker / packageName := packageName.value

    Docker / version := version.value

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
The files from ``Docker / mappings`` are extracted underneath this directory.

.. code-block:: scala

  Docker / defaultLinuxInstallLocation := "/opt/docker"

Daemon User
~~~~~~~~~~~
By default, sbt Native Packager will create a daemon user named ``demiourgos728``
whose UID is set to ``1001``, and and emit ``USER 1001`` since running as non-root is considered the best practice.

The following can be used to emit ``USER daemon`` instead:

.. code-block:: scala

    Docker / daemonUserUid := None
    Docker / daemonUser    := "daemon"

File Permission
~~~~~~~~~~~~~~~
By default, the working directory inside the Docker image is given read-only file permissions
set using multi-stage Docker build, which requires Docker 17.5 or later (watch out if you're using older Minikube).

If you want to make the working directory writable by the running process, here's the setting:

.. code-block:: scala

    import com.typesafe.sbt.packager.docker.DockerChmodType

    dockerChmodType := DockerChmodType.UserGroupWriteExecute

By default, the shell scripts generated by SBT Native Packager are given ``chmod +x`` rights. Here's the setting to do so for other files:

.. code-block:: scala

    import com.typesafe.sbt.packager.docker.DockerChmodType

    dockerAdditionalPermissions += (DockerChmodType.UserGroupPlusExecute, "/opt/docker/bin/hello")

If you don't want SBT Native Packager to change the file permissions at all here's a strategy you can choose:

.. code-block:: scala

    import com.typesafe.sbt.packager.docker.DockerPermissionStrategy

    dockerPermissionStrategy := DockerPermissionStrategy.None

This will inherit the file mode bits set in your machine. Given that Kubernetes implementations like OpenShift will use an arbitrary user,
remember to set both the user bits and group bits when running ``chmod`` yourself.

Custom Dockerfile
~~~~~~~~~~~~~~~~~

All settings before are used to create a single sequence of docker commands.
You have the option to write all of them on your own, filter or change existing
commands or simply add some.

First of all you should take a look what you docker commands look like.
In your sbt console type

.. code-block:: bash

    > show dockerCommands
    [info] List(Cmd(FROM,openjdk:8), Cmd(LABEL,MAINTAINER=Your Name <y.n@yourcompany.com>), ...)



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
  dockerCommands += Cmd("USER", (Docker / daemonUser).value)

  // use ++= to merge a sequence with an existing sequence
  dockerCommands ++= Seq(
    // setting the run script executable
    ExecCmd("RUN",
      "chmod", "u+x",
       s"${(Docker / defaultLinuxInstallLocation).value}/bin/${executableScriptName.value}"),
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
    Cmd("FROM", "openjdk:8"),
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

Init support
~~~~~~~~~~~~

By default, Java will run with PID 1 when you run your docker container. The JVM behaves differently when its PID is 1
compared to other PIDs, most notably, it doesn't respond to some signals. These include the signals usually used to
instruct a Java process to dump its threads or its heap. If you want to be able to debug a running Java container, the
inability to take thread or heap dumps can be a problem.

Docker has a convenient solution to this, it can configure a separate init process for you. This process will start
your Java process, and it will also do some other useful things that init processes are meant to do like cleaning up
orphaned processes in the container. But most importantly it will ensure that your Java process is not PID 1, which
will in turn ensure that your Java process is able to respond to signals for debugging. The command docker uses is
`tini <https://github.com/krallin/tini>`_, which as its name suggests, is tiny, only 23kb in size.

To tell docker to configure a separate init process using tini, set the `dockerBuildInit` setting to `true`:

.. code-block:: scala

  dockerBuildInit := true
