.. _debian-plugin:

Debian Plugin
=============

The debian package specification is very robust and powerful.  If you wish to do any advanced features, it's best to understand how
the underlying packaging system works.  `Debian Binary Package Building HOWTO`_ by Chr. Clemens Lee is an excellent tutorial.

.. _Debian Binary Package Building HOWTO: http://tldp.org/HOWTO/html_single/Debian-Binary-Package-Building-HOWTO/


SBT Native Packager provides two ways to build debian packages. A native one, where you need ``dpkg-deb`` installed
or a java, platform independent approach with `jdeb <https://github.com/tcurdt/jdeb>`_. By default the *native* implementation
is activated.

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

  sbt debian:packageBin

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


Enable the debian plugin to activate the native package implementation.

.. code-block:: scala

  enablePlugins(DebianPlugin)

Native packaging
~~~~~~~~~~~~~~~~

Since JARs are by default already compressed, `DebianPlugin` disables additional compression of the debian package
contents.

To compress the debian package, override `debianNativeBuildOptions` with
`options <http://man7.org/linux/man-pages/man1/dpkg-deb.1.html>`_ for `dpkg-deb`.

.. code-block:: scala

  debianNativeBuildOptions in Debian := Nil // dpkg-deb's default compression (currently xz)

  debianNativeBuildOptions in Debian := Seq("-Zgzip", "-z3") // gzip compression at level 3

Note that commit cee091c released in 1.1.1 disables package re-compression by
default. While this works great with tools such as apt and dpkg, un-compressed
package installation is `bugged in python-apt 8.8 series
<https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=718330>`_. This bug prevents
installation of the generated debian package in the following configuration:

- installation using python-apt module, used by Ansible and SaltStack for
  example,
- being on python-apt 8.8 series, that's on Debian Wheezy and perhaps older

It will fail with an error message like::

    E: This is not a valid DEB archive, it has no 'data.tar.gz', 'data.tar.bz2' or 'data.tar.lzma' member

Solutions include:

- upgrading to Debian Jessie,
- upgrading python-apt, note that no official backport is known
- re-enabling package re-compression in sbt-native-packager, by overridding
  `debianNativeBuildOptions` as described above.

Java based packaging
~~~~~~~~~~~~~~~~~~~~

If you want to use the java based implementation, enable the following plugin.

.. code-block:: scala

  enablePlugins(JDebPackaging)

and this to your ``plugins.sbt``

.. code-block:: scala

  libraryDependencies += "org.vafer" % "jdeb" % "1.3" artifacts (Artifact("jdeb", "jar", "jar"))

JDeb is a provided dependency so you have to add it on your own. It brings a lot of dependencies
that could slow your build times. This is the reason the dependency is marked as provided.



Configurations
--------------

Settings and Tasks inherited from parent plugins can be scoped with ``Debian``.

.. code-block:: scala

  linuxPackageMappings in Debian := linuxPackageMappings.value


Settings
--------

Debian requires the following specific settings:

  ``name in Debian``
    The name of the package for debian (if different from general linux name).

  ``version in Debian``
    The debian-friendly version of the package.   Should be of the form ``x.y.z-build-aa``.

  ``debianPackageConflicts in Debian``
    The list of debian packages that this package conflicts with.

  ``debianPackageDependencies in Debian``
    The list of debian packages that this package depends on.

  ``debianPackageProvides in Debian``
    The list of debian packages that are provided by this package.

  ``debianPackageRecommends in Debian``
    The list of debian packages that are recommended to be installed with this package.

  ``linuxPackageMappings in Debian``
    Debian requires a ``/usr/share/doc/{package name}/changelog.gz`` file that describes
    the version changes in this package. These should be appended to the base linux versions.

  ``maintainerScripts in Debian`` (``debianMaintainerScripts``)
    *DEPRECATED* use ``maintainerScripts in Debian`` instead.
    These are the packaging scripts themselves used by ``dpkg-deb`` to build your debian.  These
    scripts are used when installing/uninstalling a debian, like prerm, postinstall, etc.  These scripts
    are placed in the ``DEBIAN`` file when building.    Some of these files can be autogenerated,
    for example when using a package archetype, like server_application.  However, any autogenerated file
    can be overridden by placing your own files in the ``src/debian/DEBIAN`` directory.

  ``changelog in Debian``
    This is the changelog used by ``dpkg-genchanges`` to create the .changes file. This will allow you to
    upload the debian package to a mirror.


Tasks
-----

The Debian support grants the following commands:

  ``debian:package-bin``
    Generates the ``.deb`` package for this project.

  ``debian:lintian``
    Generates the ``.deb`` file and runs the ``lintian`` command to look for issues in the package.  Useful for debugging.

  ``debian:gen-changes``
    Generates the ``.changes``, and therefore the ``.deb`` package for this project.


Customize
---------------

This section contains example on how you can customize your debian build.

Customizing Debian Metadata
~~~~~~~~~~~~~~~~~~~~~~~~~~~

A Debian package provides metadata, which includes **dependencies** and **recommendations**.
A basic example to depend on java and recommend a git installation.

.. code-block:: scala

    debianPackageDependencies in Debian ++= Seq("java2-runtime", "bash (>= 2.05a-11)")

    debianPackageRecommends in Debian += "git"

To hook into the debian package lifecycle (https://wiki.debian.org/MaintainerScripts) you
can add ``preinst`` , ``postinst`` , ``prerm`` and/or ``postrm`` scripts. Just place them into
``src/debian/DEBIAN``. Or you can do it programmatically in your ``build.sbt``

.. code-block:: scala

    import DebianConstants._
    maintainerScripts in Debian := maintainerScriptsAppend((maintainerScripts in Debian).value)(
      Preinst -> "echo 'hello, world'",
      Postinst -> s"echo 'installed ${(packageName in Debian).value}'"
    )

The helper methods can be found in `MaintainerScriptHelper Scaladocs`_.

If you use the ``JavaServerAppPackaging`` there are predefined ``postinst`` and
``preinst`` files, which start/stop the application on install/remove calls. Existing
maintainer scripts will be extended not overridden.

Your control scripts are in a different castle.. directory? No problem.

.. code-block:: scala

    debianControlScriptsDirectory <<= (sourceDirectory) apply (_ / "deb" / "control")

.. _MaintainerScriptHelper Scaladocs: http://www.scala-sbt.org/sbt-native-packager/latest/api/#com.typesafe.sbt.packager.MaintainerScriptHelper$
