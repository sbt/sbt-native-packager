Debian
======
The debian package specification is very robust and powerful.  If you wish to do any advanced features, it's best to understand how
the underlying packaging system works.  http://tldp.org/HOWTO/html_single/Debian-Binary-Package-Building-HOWTO/ is an excellent tutorial.


Settings
--------

Debian requires the following specific settings:

  ``name in Debian``
    The name of the package for debian (if different from general linux name).

  ``version in Debian``
    The debian-friendly version of the package.   Should be of the form ``x.y.z-build-aa``.

  ``debianPackageDependencies in Debian``
    The list of debian packages that this package depends on.

  ``debianPackageRecommends in Debian``
    The list of debian packages that are recommended to be installed with this package.

  ``linuxPackageMappings in Debian``
    Debian requires a ``/usr/share/doc/{package name}/changelog.gz`` file that describes
    the version changes in this package. These should be appended to the base linux versions.


Tasks
-----

The Debian support grants the following commands:

  ``debian:package-bin``
    Generates the ``.deb`` package for this project.

  ``debian:lintian``
    Generates the ``.deb`` file and runs the ``lintian`` command to look for issues in the package.  Useful for debugging.
