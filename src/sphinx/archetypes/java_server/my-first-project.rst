My First Packaged Server Project
################################

Follow the instructions for the basic ``java_application`` setup in :doc:`../java_app/index` to get a working build and
understand the core concepts of sbt-native-packager. Based on this configuration we exchange enable in our ``build.sbt``


.. raw:: html

  <div class="row">
    <div class="col-md-6">

Version 1.0 or higher with sbt 0.13.5 and and higher

.. code-block:: scala

  enablePlugins(JavaServerAppPackaging) // instead of JavaAppPackaging

.. raw:: html

    </div><!-- v1.0 -->
    <div class="col-md-6">
    
Version 0.8 or lower

.. code-block:: scala

    import com.typesafe.sbt.SbtNativePackager._
    import NativePackagerKeys._
    
    packageArchetype.java_server // instead of java_application

.. raw:: html

    </div><!-- v0.8 -->
  </div><!-- row end -->


which will activate all server specific settings. As the server settings are dependent
on which OS your using the following sections will provide details for each supported
OS.

Linux
*****

A basic ``build.sbt`` for the supported ``rpm`` and ``deb`` packaging systems
require the following information:

.. code-block:: scala

    maintainer in Linux := "John Smith <john.smith@example.com>"

    packageSummary in Linux := "A small package summary"

    packageDescription := "A longer description of your application"
    

There are additional parameters available to configure. 

.. code-block:: scala

    daemonUser in Linux := normalizedName.value         // user which will execute the application
    
    daemonGroup in Linux := (daemonUser in Linux).value // group which will execute the application
    

The archetype will automatically append/prepend the creation/deletion of the user
to your packaging for Debian.  *Note:* All specified users are **deleted** on an ``apt-get purge <dpkg>``.

.. raw:: html

  <div class="alert alert-warning" role="alert">
    <span class="glyphicon glyphicon-info-sign" aria-hidden="true"></span>
    It is not a good idea to use <strong>root</strong> as the <code>appUser</code> for services as it represents a security risk.
  </div>


Default Mappings
================

The ``java_server`` archetype creates a default package structure with the following access
rights. **<package>** is a placeholder for your actual application name. By default this is
``normalizedName``.

===============================  ======  ===========  =======
Folder                           User    Permissions  Purpose
===============================  ======  ===========  =======
/usr/share/**<package>**         root    755 / (655)  static, non-changeable files
/etc/default/**<package>**       root    644          default config file
/etc/**<package>**               root    644          config folder -> link to /usr/share/**<package-name>**/conf
/var/run/**<package>**           daemon  644          if the application generates a pid on its own
/var/log/**<package>**           daemon  644          log folder -> symlinked from /usr/share/**<package>**/log
===============================  ======  ===========  =======

You can read more on best practices on `wikipedia filesystem hierarchy`_, `debian policies`_ and in
this `native packager discussion`_.

.. _wikipedia filesystem hierarchy: http://en.wikipedia.org/wiki/Filesystem_Hierarchy_Standard
.. _debian policies: https://www.debian.org/doc/debian-policy/ch-files.html
.. _native packager discussion: https://github.com/sbt/sbt-native-packager/pull/174

If you want to change something in this predefined structure read more about it in
the :doc:`linux section </formats/linux>`.

Debian (.deb)
=============

A basic ``build.sbt`` for debian requires only the Linux settings. You can build your
server application with

::

    debian:packageBin
    

Ubuntu provides two different bootsystems, SystemV and Upstart (default). To switch between
both you can add this to your ``build.sbt``

.. code-block:: scala

    import com.typesafe.sbt.packager.archetypes.ServerLoader.{SystemV, Upstart}
    
    serverLoading in Debian := SystemV
    
RPM (.rpm)
==========

A basic ``build.sbt`` for rpm requires the Linux settings and

.. code-block:: scala
    
    rpmVendor := "Your organization Inc."
    
Build your rpm package with ::

    rpm:packageBin
    
The output is partially on ``stderr`` which is a bit confusing. If the build
ends with *success* you are fine.

Windows
*******

Planned for 0.8.0

Docker
******

A basic ``build.sbt`` for Docker requires the ``linux.Keys.maintainer`` setting:


.. code-block:: scala

    maintainer in Docker := "John Smith <john.smith@example.com>"


There are a number of other available settings:

.. code-block:: scala

    daemonUser in Docker := normalizedName.value // user in the Docker image which will execute the application (must already exist)

    dockerBaseImage := "dockerfile/java" // Docker image to use as a base for the application image

    dockerExposedPorts in Docker := Seq(9000, 9443) // Ports to expose from container for Docker container linking

    dockerExposedVolumes in Docker := Seq("/opt/docker/logs") // Data volumes to make available in image

    dockerRepository := Some("dockerusername") // Repository used when publishing Docker image

A directory with appropriate contents for building a Docker image can be created with ::

  docker:stage

To build an image and store it in the local Docker server, use ::

  docker:publishLocal

To build an image, publish locally, and then push to a remote Docker repository, use ::

  docker:publish


Next, let's look at how to :doc:`customize a java server application <customize>`.


