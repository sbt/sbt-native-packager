.. _GettingStarted:

.. contents:: 

Installation
============

The sbt-native-packager is a plugin. To use it, first create a ``project/plugins.sbt`` file with the following content. 

.. code-block:: scala

  addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "x.y.z")


Also, each operating system requires its own tools. These tools are specified
in the operating system specific sections.

Version 1.0 and greater
-----------------------

If you use sbt 0.13.5 or greater you can
enable sbt native packager by enabling it in your``build.sbt``

.. code-block:: scala

  enablePlugins(SbtNativePackager)
  
  
The autoplugins mechanism will import everything automatically.

Build.scala
~~~~~~~~~~~

If you use a ``Build.scala`` you can import the available keys
with this statement

.. code-block:: scala

  import com.typesafe.sbt.SbtNativePackager.autoImport._


Version 0.8.x or lower
----------------------


If you don't use autoplugins you need to import the available
keys yourself. In your ``build.sbt`` or ``Build.scala`` add

.. code-block:: scala

  import com.typesafe.sbt.SbtNativePackager._
  import NativePackagerKeys._
  


Packaging Formats
=================


Windows
-------

Creating Windows ``msi`` packages requires the use of the Windows Installer Xml utilities.  These are available for download here: http://wix.sourceforge.net/downloadv35.html.  The plugin has only been tested with version 3.5+.


RedHat/RPM-based linux distros
------------------------------

Creating ``rpm`` packages requires the use of the following command line tools:

- ``rpmbuild``
- ``rpm`` (optional)
- ``rpmlint`` (optional)


Debian-based linux distros
--------------------------

Creating ``deb`` packages requires the use of the following command line tools:

- ``dpkg-deb``
- ``fakeroot``
- ``chmod``
- ``lintian`` (optional)

Universal
---------

Creating ``tgz`` or ``txz`` requires the use of the following command line tools:

- ``gzip``
- ``xz``
- ``tar``

Docker
------

Creating Docker images requires the use of the following command line tools:

- ``docker``

It is currently not possible to provide authentication for Docker repositories from within the build. The ``docker`` binary used by the build should already have been configured with the appropriate authentication details.
See https://docs.docker.com/reference/commandline/cli/#login.
