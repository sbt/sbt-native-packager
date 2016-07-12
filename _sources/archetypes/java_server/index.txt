.. _java-server-plugin:

Java Server Application Archetype
#################################

.. hint:: Supports only **deb** and **rpm** packaging. No support for Windows or OSX

In the :ref:`java-app-plugin` section we described how to build and customize settings related to an application.
The server archetype adds additional features you may need when running your application as a service on a server.
SBT Native Packager ships with a set of predefined install and uninstall scripts for various platforms and service
managers.


Features
========

The *JavaServerAppPackaging* archetype depends on the :ref:`java-app-plugin` and adds the following features

* install/uninstall services
* default mappings for server applications
  * ``/var/log/<pkg>`` is symlinked from ``<install>/logs``
  * ``/var/run/<pkg>`` owned by ``daemonUser``
* Creates a start script in for the configured system loader (SystemV, Upstart or SystemD)


Usage
=====

.. code-block:: scala

  enablePlugins(JavaServerAppPackaging)

Everything else works the same way as the :ref:`java-app-plugin`.

Settings & Tasks
================

This is a non extensive list of important settings and tasks this plugin provides. All settings
have sensible defaults.

  ``daemonUser``
    User to start application daemon

  ``daemonUserUid``
    UID of daemonUser

  ``daemonGroup``
    Creates or discovers the bat script used by this project.

  ``daemonGroupGid``
    GID of daemonGroup

  ``daemonShell``
    Shell provided for the daemon user

  ``serverLoading``
    Loading system to be used for application start script (SystemV, Upstart, Systemd)

  ``startRunlevels``
    Sequence of runlevels on which application will start up

  ``stopRunlevels``
    Sequence of runlevels on which application will stop

  ``requiredStartFacilities``
    Names of system services that should be provided at application start

  ``requiredStopFacilities``
    Names of system services that should be provided at application stop

  ``daemonStdoutLogFile``
    Filename stdout/stderr of application daemon. Now it's supported only in SystemV


Default Mappings
================

The java server archetype creates a default package structure with the following access rights. **<package>** is a
placeholder for your actual application name. By default this is ``normalizedName``.

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

Service Managers
================

Platforms are tied to both package managers (Rpm, Debian) and Service Managers (System V, Upstart, SystemD). By
default the native packager will configure a service manager to run the daemon process. The default configurations are:

+---------------+--------------------+
| Package Format|  Service Manager   |
+===============+====================+
| Debian        | Upstart (Default)  |
+---------------+--------------------+
| Rpm           | SystemV (Default)  |
+---------------+--------------------+

See the :ref:`server-app-customize` section on how to change these.

.. _server-app-customize:

Customize
=========

.. toctree::
   :maxdepth: 1

   customize.rst
