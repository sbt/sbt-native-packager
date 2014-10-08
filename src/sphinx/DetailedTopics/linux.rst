.. _Linux:

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

Most of the work in generating a linux package is constructing package mappings.  These 'map' a file to a location on disk where it should
reside as well as information about that file. Package mappings allow the specification of file ownership, permissions and whether or not
the file can be considered "configuration".  

  Note that while the ``sbt-native-packager`` plugin allows you to specify all of this information, not all platforms will make use of the
  information.  It's best to be specific about how you want files handled and run tests on each platform you wish to deploy to.

A package mapping takes this general form

.. code-block:: scala

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
    


The LinuxPackageMapping Models
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

All classes are located in the ``com.typesafe.sbt.packager.linux`` package. So if you want to create
instances yourself you have to add ``import com.typesafe.sbt.packager.linux._`` to your build file.

A ``LinuxPackageMapping`` contains the following fields:

  ``mappings: Traversable[(File, String)]``
    A list of mappings aggregated by this LinuxPackageMapping
    
  ``fileData: LinuxFileMetaData``
    Permissions for all the defined mappings. Default to "root:root 755"
    
  ``zipped: Boolean``
    Are the mappings zipped. Default to false
    
All mappings are stored in the task ``linuxPackageMappings`` which returns a ``Seq[LinuxPackageMapping]``. To display the contents
open the sbt console and call

.. code-block:: bash

    show linuxPackageMappings
    

The ``LinuxFileMetaData`` has the following fields

  ``user: String``
    The user owning all the mappings. Default "root"
    
  ``group: String``
    The group owning all the mappings. Default "root"
    
  ``permissions: String``
    Access permissions for all the mappings. Default "755"
    
  ``config: String``
    Are the mappings config files. Default "false"
    
  ``docs: Boolean``
    Are the mappings docs. Default to false
    
Last but not least there are the ``linuxPackageSymlinks``, which encapsulate symlinks on your
destination system. A ``LinuxSymlink`` contains only  two fields

  ``link: String``
    The actual link that points to ``destination``
    
  ``destination: String``
    The link destination
    
You can see all currently configured symlinks with this simple command. 
``linuxPackageSymlinks`` is just a ``Seq[LinuxSymlink]``
    
.. code-block:: bash

    show linuxPackageSymlinks
    
    
Modifying Mappings in General
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Adding, filtering and altering mappings are always simple methods on a sequence: ``Seq[LinuxPackageMapping]``.
The basic contstruct for adding looks like this

.. code-block:: scala

    // simple
    linuxPackageMappings += packageMapping( (theFile, "/absolute/path/somefile.txt") )
    
    // specialized
    linuxPackageMappings += packageMapping( (theFile, "/absolute/path/somefile.txt") ) withPerms("644") asDocs()
    
If you want to filter or alter things. The example has a lot of things you can _possibly_ do. Just pick
what you need. After this section there are smaller examples, showing how you can implemenet certain functions.

.. code-block:: scala

    // sbt 0.13.0 syntax
    linuxPackageMappings := {
        // mappings: Seq[LinuxPackageMapping]
        val mappings = linuxPackageMappings.value
        // this process will must return another Seq[LinuxPackageMapping]
        mappings map {  linuxPackage => 
            // basic scala collections operations. Seq[(java.io.File, String)]
            val filtered = linuxPackage.mappings map { 
                case (file, name) => file -> name // altering stuff here
            } filter { 
                case (file, name) => true // remove stuff from mappings
            }
            // case class copy method. Specify only what you need
            val fileData = linuxPackage.fileData.copy(
                user = "new user",
                group = "another group",
                permissions = "444",
                config = "false",
                docs = false
            )
            // case class copy method. Specify only what you need.
            // returns a fresh LinuxPackageMapping
            linuxPackage.copy(
                mappings = filtered,
                fileData = fileData
            )
        } filter { 
            linuxPackage => linuxPackage.mappings.nonEmpty // remove stuff. Here all empty linuxPackageMappings
        }    
    }
    
    // sbt 0.12.x syntax
    linuxPackageMappings <<= linuxPackageMappings map { mappings => 
        /* stuff. see above */ 
        mappings 
    }

The ordering in which you apply the tasks is important. 

Add Mappings
~~~~~~~~~~~~

To add an arbitrary file in your build path 

.. code-block:: scala

    linuxPackageMappings += {
      val file = sourceDirectory.value / "resources" / "somefile.txt"
      packageMapping( (file, "/absolute/path/somefile.txt") )
    }

``linuxPackageMappings`` can be scoped to ``Rpm` or ``Debian`` if you want to add mappings only for a single packacking type.

.. code-block:: scala

    linuxPackageMappings in Debian += {
      val file = sourceDirectory.value / "resources" / "debian-somefile.txt"
      packageMapping( (file, "/absolute/path/somefile.txt") )
    }
    
    linuxPackageMappings in Rpm += {
      val file = sourceDirectory.value / "resources" / "rpm-somefile.txt"
      packageMapping( (file, "/absolute/path/somefile.txt") )
    }


Filter/Remove Mappings
~~~~~~~~~~~~~~~~~~~~~~

If you want to remove some mappings you have to filter the current list of ``linuxPackageMappings``.
As ``linuxPackageMappings`` is a task, the order of your settings is important. Here are some examples
on how to filter mappings.

.. code-block:: scala

    // this is equal to 
    // linuxPackageMappings <<= linuxPackageMappings map { mappings => /* stuff */ mappings }
    linuxPackageMappings := { 
        // first get the current mappings. mapping is of type Seq[LinuxPackageMapping]
        val mappings = linuxPackageMappings.value
        // map over the mappings if you want to change them
        mappings map { mapping =>
            // we remove everything besides files that end with ".conf"
            val filtered = mapping.mappings filter {
                case (file, name) => name endsWith ".conf"
            }
            // now we copy the mapping but replace the mappings
            mapping.copy(mappings = filtered)
        } filter {
            // remove all LinuxPackageMapping instances that have to file mappings
            _.mappings.nonEmpty
        } 
    }
    
Alter LinuxPackageMapping
~~~~~~~~~~~~~~~~~~~~~~~~~

First we alter the permissions for all ``LinuxPackageMapping``s that match a specific criteria.

.. code-block:: scala

    // Altering permissions for configs
    linuxPackageMappings := {
        val mappings = linuxPackageMappings.value
        // Changing the group for all configs
        mappings map { 
            case linuxPackage if linuxPackage.fileData.config equals "true" =>
                // altering the group
                val newFileData = linuxPackage.fileData.copy(
                    group = "appdocs"
                )
                // altering the LinuxPackageMapping
                linuxPackage.copy(
                    fileData = newFileData
                )
            case linuxPackage => linuxPackage
        }
    }

Alter LinuxSymlinks
~~~~~~~~~~~~~~~~~~~

First we alter the permissions for all ``LinuxPackageMapping``s that match a specific criteria.

.. code-block:: scala

    // The same as linuxPackageMappings
    linuxPackageSymlinks := {
        val links = linuxPackageSymlinks.value
        
        links filter { /* remove stuff */ } map { /* change stuff */}
    }

.. toctree::
   :maxdepth: 2
   
   debian.rst
   redhat.rst
