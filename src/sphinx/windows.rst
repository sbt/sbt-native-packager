Windows
=======

The windows packaging is completely tied to the WIX installer toolset.  It's improtant to understand how WIX works.  http://wix.tramontana.co.hu/ is an excellent tutorial to how to create packages using wix.

Settings
--------

  ``name in Windows``
    The name of the generated msi file.

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

