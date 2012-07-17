Writing Linux Packages
======================

The native packager plugin is designed so that linux packages look similar, but can contain distribution specific information.  

Settings
--------
The required fields for any linux distribution are:

  ``name in Linux``
    The name given the package for installation.

  ``maintainer``
    The name of the maintainer of the package (important for ownership and signing).

  ``packageSummary``
    A one-sentence short summary of what the package does.

  ``packageDescription``
    A longer description of what the package does and what it includes.

  ``linuxPackageMappings``
    A list of files and their desired installation locations for the package, as well as other metainformation.


Package Mappings
----------------

Most of the work in generating a linux package is constructing package mappings.  These 'map' a file to a location on disk where it should reside as well as information about that file.   Package mappings allow the specification of file ownership, permissions and whether or not the file can be considered "configuration".  

  Note that while the ``sbt-native-packager`` plugin allows you to specify all of this information, not all platforms will make use of the information.  It's best to be specific
  about how you want files handled and run tests on each platform you wish to deploy to.

A package mapping takes this general form ::

    (packageMapping(
        file -> "/usr/share/man/man1/sbt.1.gz"
      ) withPerms "0644" gzipped) asDocs()


Let's look at each of the methods supported in the packageMapping 'library'.


  ``packageMapping(mappings: (File, String)*)``
    This method takes a variable number of ``File -> String`` pairs.  The ``File`` should be a locally available file that can be bundled, 
    and the ``String`` is the installation location on disk for that file.  This returns a new ``PackageMapping`` that supports the remaining methods.

  ``withPerms(mask: String)``
    This function adjusts the installation permissions of the associated files.  The flags passed should be of the form of a mask, e.g. ``0755``.  

  ``gzipped``
    This ensures that the files are written in compressed format to the destination.  This is a convenience for distributions that want files zipped.

  ``asDocs``
    This denotes that the mapped files are documentation files.  *Note: I believe these are only used for ``RPM``s.*

  ``withConfig(value:String="true")``
    This denotes whether or not a ``%config`` attribute is attached to the given files in the generated rpm SPEC.  Any value other than ``"true"`` will be
    placed inside the ``%config()` definition, for example ``withConfig("noreplace")`` results in ``%config(noreplace)`` attribute in the rpm spec.

  ``withUser(user:String)``
    This denotes which user should be the owner of the given files in the resulting package.

  ``withGroup(group:String)``
    This denotes which group should be the owner of the given files in the resulting package.


.. toctree::
   :maxdepth: 2
   
   debian.rst
   redhat.rst
