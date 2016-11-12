.. _windows-plugin:

Windows Plugin
==============

The windows packaging is completely tied to the WIX installer toolset.  For any non-trivial package,
it's important to understand how WIX works.  http://wix.tramontana.co.hu/ is an excellent tutorial
to how to create packages using wix.

However, the native-packager provides a simple layer on top of wix that *may* be enough for most projects.
If it is not, just override ``wixConfig`` or ``wixFile`` settings.  Let's look at the layer above direct
xml configuration.

.. note:: The windows plugin depends on the :ref:`universal-plugin`.

Requirements
------------

You need the following applications installed

* `WIX Toolset <http://wixtoolset.org/>`_

Build
-----

.. code-block:: bash

  sbt windows:packageBin

Required Settings
~~~~~~~~~~~~~~~~~

A windows package needs some mandatory settings to be valid. Make sure
you have these settings in your build:

.. code-block:: scala

  // general package information (can be scoped to Windows)
  maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"
  packageSummary := "test-windows"
  packageDescription := """Test Windows MSI."""

  // wix build information
  wixProductId := "ce07be71-510d-414a-92d4-dff47631848a"
  wixProductUpgradeId := "4552fb0e-e257-4dbd-9ecb-dba9dbacf424"


1.0 or higher
~~~~~~~~~~~~~

Enables the windows plugin

.. code-block:: scala

  enablePlugins(WindowsPlugin)


0.8 or lower
~~~~~~~~~~~~

For this versions windows packaging is automatically activated.
See the :doc:`Getting Started </gettingstarted>` page for information
on how to enable sbt native packager.

Configuration
-------------

Settings and Tasks inherited from parent plugins can be scoped with ``Universal``.

.. code-block:: scala

  mappings in Windows := (mappings in Universal).value

Now, let's look at the full set of windows settings.

Settings
--------

  ``name in Windows``
    The name of the generated msi file.

  ``candleOptions``
    the list of options to pass to the ``candle.exe`` command.

  ``lightOptions``
    the list of options to pass to the ``light.exe`` command.  Most likely setting is: ``Seq("-ext", "WixUIExtension", "-cultures:en-us")`` for UI.

  ``wixProductId``
    The GUID to use to identify the windows package/product.

  ``wixProductUpgradeId``
    The GUID to use to identify the windows package/product *upgrade* identifier (see wix docs).

  ``wixPackageInfo``
    The information used to autoconstruct the ``<Product><Package/>`` portion of the wix xml.  **Note: unused if ``wixConfig`` is overridden**

  ``wixProductLicense``
    An (optional) ``rtf`` file to display as the product license during installation.  Default to looking for ``src/windows/License.rtf``

  ``wixFeatures``
    A set of windows features that users can install with this package.  **Note: unused if ``wixConfig`` is overridden**

  ``wixProductConfig``
    inline XML to use for wix configuration.  This is everything nested inside the ``<Product>`` element.

  ``wixConfig``
    inline XML to use for wix configuration.   This is used if the ``wixFile`` setting is not specified.

  ``wixFile``
    The file containing WIX xml that defines the build.

  ``mappings in packageMsi in Windows``
    A list of file->location pairs.   This list is used to move files into a location where WIX can pick up the files and generate a ``cab`` or embedded ``cab`` for the ``msi``.
    The WIX xml should use the relative locations in this mappings when references files for the package.

Tasks
-----

  ``windows:packageBin``
    Creates the ``msi`` package.

  ``wix-file``
    Generates the Wix xml file from `wixConfig` and `wixProductConfig` setings, unless overriden.


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


Customize
---------

Feature configuration
~~~~~~~~~~~~~~~~~~~~~

The abstraction over wix allows you to configure "features" that users may optionally install. These feature are higher level things,
like a set of files or menu links. The currently supported components of features are:

1. Files (``ComponentFile``)
2. Path Configuration (``AddDirectoryToPath``)
3. Menu Shortcuts (``AddShortCuts``)


To create a new feature, simple instantiate the ``WindowsFeature`` class with the desired feature components that are included.

Here's an example feature that installs a binary and a script, as well as path settings:

.. code-block:: scala

    wixFeatures += WindowsFeature(
        id="BinaryAndPath",
        title="My Project's Binaries and updated PATH settings",
        desc="Update PATH environment variables (requires restart).",
        components = Seq(
          ComponentFile("bin/cool.bat"),
          ComponentFile("lib/cool.jar"),
          AddDirectoryToPath("bin"))
    )

All file references should line up exactly with those found in the ``mappings in Windows`` configuration.   When generating an MSI, the plugin will first create
a directory using all the ``mappings in Windows`` and configure this for inclusion in a ``cab`` file.  If you'd like to add files to include, these must *first*
be added to the mappings, and then to a feature.   For example, if we complete the above setting to include file mappings, we'd have the following:

.. code-block:: scala

    mappings in Windows ++= (packageBin in Compile, sourceDirectory in Windows) map { (jar, dir) =>
      Seq(jar -> "lib/cool.jar", (dir / "cool.bat") -> "bin/cool.bat")
    }

    wixFeatures += WindowsFeature(
        id="BinaryAndPath",
        title="My Project's Binaries and updated PATH settings",
        desc="Update PATH environment variables (requires restart).",
        components = Seq(
          ComponentFile("bin/cool.bat"),
          ComponentFile("lib/cool.jar"),
          AddDirectoryToPath("bin"))
    )

Right now this layer is *very* limited in what it can accomplish, and hasn't been heavily debugged.  If you're interested in helping contribute, please
do so!   However, for most command line tools, it should be sufficient for generating a basic ``msi`` that windows users can install.
