Universal
=========

Universal packaging just takes a plain ``mappings`` configuration and generates various 
package files for distribution.  It allows you to provide your users a distribution
that is not tied to any particular platform, but may require manual labor to set up.

Configurations
--------------
Universal packaging provides three Configurations:

  ``universal``
    For creating full distributions
  ``universal-docs``
    For creating bundles of documentation
  ``universal-src``
    For creating bundles of source.


Settings
--------
The Universal packages are completely configured through the use of the mappings key.  Simply
specify the desired mappings for a given configuration.  For Example:

    ``mappings in Universal <+= packageBin in Compile map { p => p -> "lib/foo.jar" }``

The different types of archives can also be configured through selection.

    ``mappings in Universal in package-zip-tarball += file("README") -> "README"``
    
Besides ``mappings``, the ``name``, ``sourceDirectory`` and ``target`` configurations are all respected.

**Note: The Universal plugin will make anything in a bin/ directory executable.  This is to work around issues with JVM and file system manipulations.**


Commands
--------

  ``universal:package-bin``
    Creates the ``zip`` universal package.
  
  ``universal:package-zip-tarball``
    Creates the ``tgz`` universal package.
    
  ``universal:package-xz-tarball``
    Creates the ``txz`` universal package.  The ``xz`` command can get better compression
    for some types of archives.
    
  ``universal-docs:package-bin``
    Creates the ``zip`` universal documentation package.
  
  ``universal-docs:package-zip-tarball``
    Creates the ``tgz`` universal documentation package.
    
  ``universal-docs:package-xz-tarball``
    Creates the ``txz`` universal documentation package.  The ``xz`` command can get better compression
    for some types of archives.