.. _archetypes:

Project Archetypes
##################

Archetype plugins provide predefined configurations for your build. Like :ref:`format plugins<packaging-formats>`,
archetype plugins can depend on other archetype plugins to extend existing functionality.

Project archetypes are default deployment scripts that try to "do the right thing" for a given type of project.
Because not all projects are created equal, there is no single archetype for all native packages, but a set
of them to choose from.

.. toctree::
   :maxdepth: 1

   Java Command Line Application <java_app/index.rst>
   Java Server Application <java_server/index.rst>
   Systemloaders <systemloaders.rst>
   Configuration Archetypes <misc_archetypes.rst>
   Jlink Plugin <jlink_plugin.rst>
   An archetype cheatsheet <cheatsheet.rst>
