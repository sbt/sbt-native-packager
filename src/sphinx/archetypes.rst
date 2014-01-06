.. _Archetypes:

Project Archetypes
==================

Project archetypes are default deployment scripts that try to "do the right thing" for a given type of project.
Because not all projects are created equal, there is no one single archetype for all native packages, but a set
of them for usage.

The architecture of the plugin is set up so that you can customize your packages at any level of complexity.  
For example, if you'd like to write Windows Installer XML by hand and manually map files, you should be able to do this while
still leveraging the default configuration for other platforms.


Curently, in the nativepackager these archetypes are available:

  * Java Command Line Application
  * Java Server Application (Experimental - Debian Only)
  

Java Command Line Application
-----------------------------

A Java Command Line application is a Java application that consists of a set of JARs and a main method.  There is no
custom start scripts, or services.  It is just a bash/bat script that starts up a Java project.   To use
this archetype in your build, do the following in your ``build.sbt``:

.. code-block:: scala

    packageArchetype.java_application

    name := "A-package-friendly-name"
    
    packageSummary in Linux := "The name you want displayed in package summaries"

    packageSummary in Windows := "The name you want displayed in Add/Remove Programs"

    packageDescription := " A description of your project"

    maintainer in Windows := "Company"
    
    maintainer in Debian := "Your Name <your@email.com>"

    wixProductId := "ce07be71-510d-414a-92d4-dff47631848a"

    wixProductUpgradeId := "4552fb0e-e257-4dbd-9ecb-dba9dbacf424"


This archetype will use the ``mainClass`` setting of sbt (automatically discovers your main class) to generate ``bat`` and ``bin`` scripts for your project.  It
produces a universal layout that looks like the following:

.. code-block:: none

    bin/
      <app_name>       <- BASH script
      <app_name>.bat   <- cmd.exe script
    lib/
       <Your project and dependent jar files here.>


You can add additional files to the project by placing things in ``src/windows``, ``src/universal`` or ``src/linux`` as needed.

The default bash script also supports having a configuration file.  This config file can be used to specify default arguments to the BASH script.
To define a config location for your bash script, you can manually override the template defines:

.. code-block:: scala

    bashScriptConfigLocation := "$app_home/conf/my.conf"


This string can include any variable defines in the BASH script. In this case, ``app_home`` refers to the install location of the script.

Java Server
-----------

This archetype is designed for Java applications that are intended to run as
servers or services.  This archetype includes wiring an application to start 
immediately upon startup.

Currently supported operating systems:

* Ubuntu 12.04 LTS - Upstart
* Ubuntu 12.04 LTS - init.d


The Java Server archetype has a similar installation layout as the java
application archetype. The primary differneces are:

* Linux

  * ``/var/log/<pkg>`` is symlinked from ``<install>/log``

  * Creates a start script in ``/etc/init.d`` or ``/etc/init/``

  * Creates a startup config file in ``/etc/default/<pkg>``


For Debian servers, you can select to either use SystemV or Upstart for your servers.  By default, Upstart (the current Ubuntu LTS default), is used.  To switch to SystemV, add the following:

.. code-block:: scala

    import NativePackagerKeys._
    import com.typesafe.sbt.packager.archetypes.ServerLoader

    serverLoading in Debian := ServerLoader.SystemV

By default, the native packager will install and run services using the ``root`` user and group.  This is not a good default for services, which should not be exposed to root access.  You can change the installation and usage user via the ``daemonUser`` key:

.. code-block:: scala

    daemonUser in Debian := "my_app_user"

The archetype will automatically append/prepend the creation/deletion of the user
to your packaging for Debian.  *Note:* All specified users are **deleted** on an ``apt-get purge <dpkg>``.




Overriding Templates
--------------------

You can override the default template used to generate any of the scripts in
any archetype.   Listed below are the overridable files and variables that
you can use when generating scripts.

``src/templates/bat-template``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Creating a file here will override the default template used to
generate the ``.bat`` script for windows distributions.

**Syntax**

``@@APP_ENV_NAME@@`` - will be replaced with the script friendly name of your package.

``@@APP_NAME@@`` - will be replaced with user friendly name of your package.

``@APP_DEFINES@@`` - will be replaced with a set of variable definitions, like
  ``APP_MAIN_CLASS``, ``APP_MAIN_CLASS``.

You can define addiitonal variable definitions using ``batScriptExtraDefines``.

``src/templates/bash-template``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Creating a file here will override the default template used to 
generate the BASH start script found in ``bin/<application>`` in the
universal distribution

**Syntax**

``${{template_declares}}`` - Will be replaced with a series of ``declare <var>``
lines based on the ``bashScriptDefines`` key.  You can add more defines to
the ``bashScriptExtraDefines`` that will be used in addition to the default set:

* ``app_mainclass`` - The main class entry point for the application.
* ``app_classpath`` - The complete classpath for the application (in order).



``src/templates/start``
~~~~~~~~~~~~~~~~~~~~~~~

Creating a file here will override either the init.d startup script or
the upstart start script.  It will either be located at
``/etc/init/<application>`` or ``/etc/init.d/<application>`` depending on which
serverLoader is being used.

**Syntax**

You can use ``${{variable_name}}`` to reference variables when writing your scirpt.  The default set of variables is:

* ``descr`` - The description of the server.
* ``author`` - The configured author name.
* ``exec`` - The script/binary to execute when starting the server
* ``chdir`` - The working directory for the server.
* ``retries`` - The number of times to retry starting the server.
* ``retryTimeout`` - The amount of time to wait before trying to run the server.
* ``app_name`` - The name of the application (linux friendly)
* ``app_main_class`` - The main class / entry point of the application.
* ``app_classpath`` - The (ordered) classpath of the application.
* ``daemon_user`` - The user that the server should run as.

``src/templates/etc-default``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Creating a file here will override the ``/etc/default/<application>`` template
used when SystemV is the server loader.
