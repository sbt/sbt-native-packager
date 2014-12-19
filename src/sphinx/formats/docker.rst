Docker Plugin
=============

Docker images describe how to set up a container for running an application, including what files are present, and what program to run.

  https://docs.docker.com/introduction/understanding-docker/ provides an introduction to Docker.
  https://docs.docker.com/reference/builder/ describes the Dockerfile; a file which describes how to set up the image.

  sbt-native-packager focuses on creating a Docker image which can "just run" the application built by SBT.
  
.. contents:: 
  :depth: 2

Requirements
------------

You need the docker console client installed. SBT Native Packager doesn't use the REST API.


Build
-----

.. code-block:: bash

  sbt docker:publishLocal
  

Required Settings
~~~~~~~~~~~~~~~~~

Docker images require the following setting:

.. code-block:: scala

    maintainer in Docker := "John Smith <john.smith@example.com>"

    
1.0 or higher
~~~~~~~~~~~~~

.. code-block:: scala

  enablePlugins(DockerPlugin)

0.8.x
~~~~~

For this versions docker packaging is automatically activated.
See the :doc:`Getting Started </gettingstarted>` page for informations
on how to enable sbt native packager.

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

  ``dockerExposedPorts in Docker``
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

    dockerExposedPorts in Docker := Seq(9000, 9443)
    
    dockerExposedVolumes in Docker := Seq("/opt/docker/logs")

Install Location
~~~~~~~~~~~~~~~~
The path to which the application is written can be changed with the setting.
The files from ``mappings in Docker`` are extracted underneath this directory.

.. code-block:: scala
  
  defaultLinuxInstallLocation in Docker := "/opt/docker"
