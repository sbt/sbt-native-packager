My First Packaged Server Project
################################

Follow the instructions for the basic ``java_application`` setup in :doc:`../GettingStartedApplications/index` to get a working build and
understand the core concepts of sbt-native-packager. Based on this configuration we exchange
in our ``build.sbt``

.. code-block:: scala

    packageArchetype.java_application
    
with

.. code-block:: scala

    packageArchetype.java_server


which will activate all server specific settings. As the server settings are dependend
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

    daemonUser in Linux := normalizedName.value // user which will execute the application
    
    daemonGroup in Linux := daemonUser.value    // group which will execute the application


Debian (.deb)
=============

A basic ``build.sbt`` for debian requires only the Linuxs settings. You can build your
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

A basic ``build.sbt`` for rpm requires the Linuxs settings and

.. code-block:: scala
    
    rpmVendor := "Your organization Inc."
    
Build your rpm package with ::

    rpm:packageBin
    
The output is partially on ``stderr`` which is a bit confusing. If the build
ends with *success* you are fine.

Windows
*******

Planned for 0.8.0

Next, let's look at how to :doc:`Add configuration files <AddingConfiguration>` to use with our script.


