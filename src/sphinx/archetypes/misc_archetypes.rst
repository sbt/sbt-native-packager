.. _config-archetypes:

Configuration Archetypes
========================

This is a small collection of additional archetypes that provide smaller enhancements.

AshScript Plugin
----------------

This class is an alternate to JavaAppPackaging designed to support the ash shell. :ref:`java-app-plugin`
generates bash-specific code that is not compatible with ash, a very stripped-down, lightweight shell
used by popular micro base Docker images like BusyBox.  The AshScriptPlugin will generate simple
ash-compatible output.

.. code-block:: scala

    enablePlugins(AshScriptPlugin)


ClasspathJar & LauncherJar Plugin
---------------------------------

See the :ref:`long-classpaths` section for usage of these plugins.

Jlink Plugin
------------

This plugin builds on Java's `jlink`_ tool to embed a JVM image (a stripped-down JRE)
into your package. It produces a JVM image containing only the modules that are referenced
from the dependency classpath.

.. code-block:: scala

  enablePlugins(JlinkPlugin)

The plugin requires Oracle JDK 11 or OpenJDK 11. Although `jlink` and `jdeps` are also
a part of the older JDK versions, those lack some of the newer features, which was not
addressed in the current plugin version.

This plugin must be run on the platform of the target installer. The tooling does *not*
provide a means of creating, say, Windows installers on MacOS, or MacOS on Linux, etc.

For further details on the capabilities of `jlink`, see the
`jlink <https://docs.oracle.com/en/java/javase/11/tools/jlink.html>`_ and
`jdeps <https://docs.oracle.com/en/java/javase/11/tools/jdeps.html>`_ references.
(Note: only some of the possible settings are exposed through this plugin. Please submit a
`Github <https://github.com/sbt/sbt-native-packager/issues>`_ issue or pull request if something specific is desired.)

