.. _packaging-formats:

Packaging Formats
#################

There is a plugin for each packaging format that native-packager supports. These plugins can rely on each other to reuse
existing functionality. Currently the autoplugin hierarchy looks like this ::

            SbtNativePackager
                  +
                  |
                  |
    +-------+  Universal  +--------+-------------+
    |             +                |             |
    |             |                |             |
    |             |                |             |
    +             +                +             +
  Docker    +-+ Linux +-+       Windows     JDKPackager
            |           |
            |           |
            +           +
          Debian       RPM


If you enable the ``DebianPlugin`` all plugins that depend on the ``DebianPlugin`` will be enabled as well (``LinuxPlugin``, ``UniversalPlugin``
and ``SbtNativePackager``).

Each packaging format defines its own scope for settings and tasks, so you can customize
your build on a packaging level. The settings and tasks must be explicitly inherited. For the ``mappings`` task this
looks like this

.. code-block:: scala

  mappings in Docker := (mappings in Universal).value


To learn more about a specific plugin, read the appropriate doc.

.. tip:: You may also need to read the docs of the dependent plugins. We recommend always to read the
    :ref:`universal-plugin` documentation as all plugins rely on this one.

.. toctree::
   :maxdepth: 1

   universal.rst
   linux.rst
   debian.rst
   rpm.rst
   docker.rst
   windows.rst
   jdkpackager.rst
