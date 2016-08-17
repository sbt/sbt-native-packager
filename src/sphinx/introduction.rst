.. _introduction:

Introduction
############

SBT native packager lets you build application packages in native formats and offers different archetypes for common
configurations, such as simple Java apps or server applications.

This section provides a general overview of native packager and its core concepts. If you want a quick start, go to the
:ref:`getting started section <getting-started>`. However we recommend understanding the core concepts, which will help
you to get started even quicker.

.. _goals:

Goals
=====

Native packager defines project goals in order to set expectations and scope for this project.

1. Native formats should build on their respective platform
    This allows native packager to support a wide range of formats as the packaging plugin serves as a wrapper around
    the actual packaging tool. However, alternative packaging plugins maybe provided if a java/scala implementation
    exists. As an example *debian* packages should always build on debian systems, however native packager provides
    an additional plugin that integrates JDeb for a platform independent packaging strategy.
2. Provide archetypes for zero configuration builds
    While packaging plugins provide the *how* a package is created, archetypes provide the configuration for *what* gets
    packaged. Archetypes configure your build to create a package for a certain purpose. While an archetype may not
    support all packaging formats, it should work without configuration for the supported formats.
3. Enforce best-practices
    There is no single way to create a package. Native packager *tries* to create packages following best practices,
    e.g. for file names, installation paths or script layouts.


.. _scope:

Scope
=====

While native packager provides a wide range of formats and archetype configurations, its scope is relatively narrow.
Native packager only takes care of *packaging*, the act of putting a list of ``mappings`` (source file to install target
path) into a distinct package format (*zip*, *rpm*, etc.).

Archetypes like :ref:`java-app-plugin` or :ref:`java-server-plugin` only add additional files to the ``mappings``
enriching the created package, but they don't provide any new features for native-packager core functionality. Much like
the :ref:`packaging format plugins <packaging-formats>`, the archetypes rely on functionality already available on your
deploy target.

These things are **out of native packagers scope**

1. Providing application lifecyle management.
    The :ref:`java-server-plugin` provides *configurations* for common systeloaders like SystemV, Upstart or SystemD.
    However create a custome solution, which includes stop scripts, PID management, etc. are not part of native
    packager.

2. Providing deployment configurations
    Native packager produces artefacts with the ``packageBin`` task. What you do with these is part of another step in
    your process.


.. _formats-and-archetypes:

Core Concepts
=============

Native packager is based on a few simple concepts. If you understand these, you will be able to customize your build,
create own packaging formats and deploy more effectively.

1. **Separation of concerns** of the two plugin kinds

    - :ref:`format plugins <packaging-formats>` define **how** a package is created
    - :ref:`archetype plugins <archetypes>` define **what** a package should contain


2. **Mappings** define how your build files should be organized on the target system.

    ``Mappings`` are a ``Seq[(File, String)]``, which translates to "a list of tuples,  where each tuple defines a source file that gets mapped to a path on the target system".


The following sections describe these concepts in more detail.

Format Plugins
~~~~~~~~~~~~~~

Format plugins provide the implementation to create package, the **how** a package is created. For example the
:ref:`debian-plugin` provides a way to package debian packages. Each format plugin has its
:ref:`own documentation <packaging-formats>`. Each plugin provides a common set of features:

1. Provide a new configuration scope
    Formats define their own configuration scope to be able to customize every shared setting or task.

2. Provide package format related settings and tasks
    Each format plugin may add additional settings or tasks that are only used by this plugin. Normally these settings
    start with the plugin name, e.g. *rpmXYZ*.

3. Implement package task
    The ``packageBin`` or ``publishLocal`` ( docker ) task provides the actual action to create a package.

By enabling only a format plugin with

.. code-block:: scala

    enablePlugins(SomePackageFormatPlugin)

the resulting package will be empty as a format plugin doesn't provide any configuration other than the default settings
for the format plugin's specific settings.


Archetype Plugins
~~~~~~~~~~~~~~~~~

While format plugins provide the **how**, archetypes provide the **what** gets packaged. They don't add configuration
scopes, but change the configuration in all supported package format scopes. A full list of archetypes can be found
:ref:`here <archetypes>`. An archetype may provide the following:

1. New, archetype related settings and tasks
2. New files in your package

By enabling an archetype plugin with

.. code-block:: scala

    enablePlugins(SomeArchetypePlugin)

all configuration changes will be applied as well as all supported format plugins will be enabled.


.. tip:: An archetype plugin should be the starting point for creating packages!

Mappings
~~~~~~~~

Mappings are the heart of native packager. This task defines what files in your build should be mapped where on the
target system. The type signature for the mappings task is

.. code-block:: scala

  mappings: TaskKey[Seq[(File, String)]]

The *file* part of the tuple must be available during the packaging phase. The String part represents the path inside
the installation directory.

The :ref:`universal-plugin` represents the base for all other plugins. It has a :ref:`big section on how to customize
mappings <universal-plugin-getting-started-with-packaging>`.

Architecture
~~~~~~~~~~~~

Native packagers architecture can be summarized with this diagram

.. image:: /static/sbt-native-packager-design.svg
    :alt: Architecture diagram.

When using the full power of the plugin, all of the packaging is driven from the ``mappings in Universal`` setting,
which defines what files will be included in the package. These files are automatically moved around for the appropriate
native packaging as needed.
