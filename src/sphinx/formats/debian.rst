.. _debian-plugin:

Debian Plugin
=============

The debian package specification is very robust and powerful.  If you wish to do any advanced features, it's best to understand how
the underlying packaging system works.  `Debian Binary Package Building HOWTO`_ by Chr. Clemens Lee is an excellent tutorial.

.. _Debian Binary Package Building HOWTO: http://tldp.org/HOWTO/html_single/Debian-Binary-Package-Building-HOWTO/


SBT Native Packager provides two ways to build debian packages:

1.  A native implementation, where you need ``dpkg-deb`` installed, or
2.  A java, platform independent approach with `jdeb <https://github.com/tcurdt/jdeb>`_.

By default the *native* implementation is activated.

.. note:: The debian plugin depends on the :ref:`linux-plugin`.

Requirements
------------

If you use the *native*  debian package implementation you need the following applications installed:

* dpkg-deb
* dpkg-sig
* dpkg-genchanges
* lintian
* fakeroot

Build
-----

.. code-block:: bash

  sbt debian/packageBin

Required Settings
~~~~~~~~~~~~~~~~~

A debian package needs some mandatory settings to be valid. Make sure
you have these settings in your build:

.. code-block:: scala

    name := "Debian Example"

    version := "1.0"

    maintainer := "Max Smith <max.smith@yourcompany.io>"

    packageSummary := "Hello World Debian Package"

    packageDescription := """A fun package description of our software,
      with multiple lines."""


It's not exactly mandatory, but still highly recommended to add
relevant :ref:`JRE dependency <jre-dependencies>`, for example:

.. code-block:: scala

    debianPackageDependencies := Seq("java8-runtime-headless")

Enable the debian plugin to activate the native package implementation.

.. code-block:: scala

  enablePlugins(DebianPlugin)

.. _jre-dependencies:

JRE Dependencies
~~~~~~~~~~~~~~~~

By default, a Debian package would have no dependencies, even for the
Java Runtime Environment (JRE). A startup script will seek JRE in
several popular locations, and, JRE is not found, the following
message would be displayed::

    No java installations was detected.
    Please go to http://www.java.com/getjava/ and download

To build a Debian package that integrates properly with Debian
repository environment, i.e. depends on a package that provides JRE,
one needs to specify JRE dependency using
``debianPackageDependencies``. Debian (Ubuntu and compatible
distributions as well) provides two families of virtual packages to do
that:

  ``javaN-runtime``
    Regular (full) JRE packages, with GUI support. Use this for
    applications requiring AWT/Swing support, OpenGL, sound, etc.

  ``javaN-runtime-headless``
    Minimal JRE packages without GUI support, useful for server
    installation to avoid pulling large set of X.org-related
    packages. Use this for console-only applications, services,
    networked / web applications, etc.

``N`` in ``javaN`` should be replaced with minimal JRE version
required by the packaged application. It usually depends on a Scala
version used:

* Scala 2.11.x or earlier requires Java 6
* Scala 2.12.x requires Java 8

Note that these are *virtual* packages, which are provided by a set of
real packages. This means, for example, while installing a .deb
package that depends on ``java6-runtime-headless``:

* If end-user has no suitable JRE installed, it would automatically
  pull and install some "sane default" package which provides thing
  functionality (typically, it would be ``openjdk-8-jre-headless``).
* If end-user does not like default suggested JRE for some reason,
  it's possible to install any alternative implementation.
* If end-user has some existing JRE installation that is sufficient to
  play that role (for example, ``openjdk-9-jre``, which provides,
  along others, ``java8-runtime-headless`` too), it would be used.

This dependency works equally well with both free/libre OpenJDK
packages supplied by Debian, and non-free JDKs supplied by Oracle and
packaged as .deb using `make-jpkg utility
<https://wiki.debian.org/JavaPackage>`_ from Debian's `java-package
<https://packages.debian.org/java-package>`_.

Native packaging
~~~~~~~~~~~~~~~~

Since JARs are by default already compressed, `DebianPlugin` disables additional compression of the debian package
contents.

To compress the debian package, override `debianNativeBuildOptions` with
`options <http://man7.org/linux/man-pages/man1/dpkg-deb.1.html>`_ for `dpkg-deb`.

.. code-block:: scala

  Debian / debianNativeBuildOptions := Nil // dpkg-deb's default compression (currently xz)

  Debian / debianNativeBuildOptions := Seq("-Zgzip", "-z3") // gzip compression at level 3

Note that commit cee091c released in 1.1.1 disables package re-compression by
default. While this works great with tools such as apt and dpkg, un-compressed
package installation is `bugged in python-apt 8.8 series
<https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=718330>`_. This bug prevents
installation of the generated debian package in the following configuration:

- installation using python-apt module, used by Ansible and SaltStack for
  example,
- being on python-apt 8.8 series that's on Debian Wheezy and perhaps older

It will fail with an error message like::

    E: This is not a valid DEB archive, it has no 'data.tar.gz', 'data.tar.bz2' or 'data.tar.lzma' member

Solutions include:

- upgrading to Debian Jessie,
- upgrading python-apt, note that no official backport is known
- re-enabling package re-compression in sbt-native-packager, by overridding
  `debianNativeBuildOptions` as described above.

Java based packaging
~~~~~~~~~~~~~~~~~~~~

If you want to use the java based implementation, enable the following plugin:

.. code-block:: scala

  enablePlugins(JDebPackaging)

and this to your ``plugins.sbt``:

.. code-block:: scala

  libraryDependencies += "org.vafer" % "jdeb" % "1.3" artifacts (Artifact("jdeb", "jar", "jar"))

JDeb is a provided dependency. You have to explicitly add it on your own. It brings a lot of dependencies
that could slow your build times. This is the reason the dependency is marked as provided.


Configurations
--------------

Settings and Tasks inherited from parent plugins can be scoped with ``Debian``.

.. code-block:: scala

  Debian / linuxPackageMappings := linuxPackageMappings.value


Settings
--------

Debian requires the following specific settings:

  ``Debian / name``
    The name of the package for debian (if different from general linux name).

  ``Debian / version``
    The debian-friendly version of the package.   Should be of the form ``x.y.z-build-aa``.

  ``Debian / debianPackageConflicts``
    The list of debian packages that this package conflicts with.

  ``Debian / debianPackageDependencies``
    The list of debian packages that this package depends on.

  ``Debian / debianPackageProvides``
    The list of debian packages that are provided by this package.

  ``Debian / debianPackageRecommends``
    The list of debian packages that are recommended to be installed with this package.

  ``Debian / linuxPackageMappings``
    Debian requires a ``/usr/share/doc/{package name}/changelog.gz`` file that describes
    the version changes in this package. These should be appended to the base linux versions.

  ``Debian / maintainerScripts`` (``debianMaintainerScripts``)
    *DEPRECATED* use ``Debian / maintainerScripts`` instead.
    These are the packaging scripts themselves used by ``dpkg-deb`` to build your debian.  These
    scripts are used when installing/uninstalling a debian, like prerm, postinstall, etc.  These scripts
    are placed in the ``DEBIAN`` file when building.    Some of these files can be autogenerated,
    for example when using a package archetype, like server_application.  However, any autogenerated file
    can be overridden by placing your own files in the ``src/debian/DEBIAN`` directory.

  ``Debian / changelog``
    This is the changelog used by ``dpkg-genchanges`` to create the .changes file. This will allow you to
    upload the debian package to a mirror.


Tasks
-----

The Debian support grants the following commands:

  ``Debian / package-bin``
    Generates the ``.deb`` package for this project.

  ``Debian / lintian``
    Generates the ``.deb`` file and runs the ``lintian`` command to look for issues in the package.  Useful for debugging.

  ``Debian / gen-changes``
    Generates the ``.changes``, and therefore the ``.deb`` package for this project.


Customize
---------------

This section contains examples of how you can customize your debian build.

Customizing Debian Metadata
~~~~~~~~~~~~~~~~~~~~~~~~~~~

A Debian package provides metadata, which includes **dependencies** and **recommendations**.
This example adds a dependency on java and recommends a git installation.

.. code-block:: scala

    Debian / debianPackageDependencies ++= Seq("java2-runtime", "bash (>= 2.05a-11)")

    Debian / debianPackageRecommends += "git"

Hook Actions into the Debian Package Lifecycle
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To hook into the debian package lifecycle (https://wiki.debian.org/MaintainerScripts) you
can add ``preinst`` , ``postinst`` , ``prerm`` and/or ``postrm`` scripts. Just place them into
``src/debian/DEBIAN``. Or you can do it programmatically in your ``build.sbt``.  This example adds actions to ``preinst`` and ``postinst``:

.. code-block:: scala

    import DebianConstants._
    Debian / maintainerScripts := maintainerScriptsAppend((Debian / maintainerScripts).value)(
      Preinst -> "echo 'hello, world'",
      Postinst -> s"echo 'installed ${(Debian / packageName).value}'"
    )

The helper methods can be found in `MaintainerScriptHelper Scaladocs`_.

If you use the ``JavaServerAppPackaging`` there are predefined ``postinst`` and
``preinst`` files, which start/stop the application on install/remove calls. Existing
maintainer scripts will be *extended* not overridden.

Use a Different Castle Directory for your Control Scripts
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Your control scripts are in a different castle.. directory? No problem.

.. code-block:: scala

    debianControlScriptsDirectory <<= (sourceDirectory) apply (_ / "deb" / "control")

.. _MaintainerScriptHelper Scaladocs: http://www.scala-sbt.org/sbt-native-packager/latest/api/#com.typesafe.sbt.packager.MaintainerScriptHelper$
