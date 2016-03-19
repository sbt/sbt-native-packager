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
