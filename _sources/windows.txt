Windows
=======

The windows packaging is completely tied to the WIX installer toolset.  It's important to understand how WIX works.  http://wix.tramontana.co.hu/ is an excellent tutorial to how to create packages using wix.

Settings
--------

  ``name in Windows``
    The name of the generated msi file.

  ``candleOptions``
    the list of options to pass to the ``candle.exe`` command.

  ``lightOptions``
    the list of options to pass to the ``light.exe`` command.  Most likely setting is: ``Seq("-ext", "WixUIExtension", "-cultures:en-us")`` for UI.

  ``wixConfig``
    inline XML to use for wix configuration.   This is used if the ``wixFile`` setting is not specified.

  ``wixFile``
    The file containing WIX xml that defines the build.

  ``mappings in packageMsi in Windows``
    A list of file->location pairs.   This list is used to move files into a location where WIX can pick up the files and generate a ``cab`` or embedded ``cab`` for the ``msi``.
    The WIX xml should use the relative locations in this mappings when references files for the package.

Commands
--------

  ``windows:package-msi``
    Creates the ``msi`` package.
  
  ``wix-file``
    Generates the Wix xml file from `wixConfig` setings, unless overriden.
    
Utilities
---------

The native-packager plugin provides a few handy utilities for generating Wix XML.  These
utilities are located in the ``com.typesafe.packager.windows.WixHelper`` object.  Among
these are the following functions:

  ``cleanStringForId(String): String``
    Takes in a string and returns a wix-friendly identifier.  Note: truncates to 50 characters.
  
  ``cleanFileName(String): String``
    Takes in a file name and replaces any ``$`` with ``$$`` to make it past the Wix preprocessor.

  ``generateComponentsAndDirectoryXml(File): (Seq[String], scala.xml.Node)``
    This method will take a file and generate ``<Directory>``, ``<Component>`` and ``<File>``
    XML elements for all files/directories contained in the given file.  It will return the
    ``Id`` settings for any generated components.  This is a handy way to package a large
    directory of files for usage in the Features of an MSI.

