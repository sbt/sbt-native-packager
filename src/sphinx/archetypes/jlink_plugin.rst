.. _jlink-plugin:

Jlink Plugin
============

This plugin builds on Java's `jlink`_ tool to embed a JVM image (a stripped-down JRE)
into your package. It produces a JVM image containing only the modules that are referenced
from the dependency classpath.

Note: Current implementation only detects the platform modules (that is, the ones present in
the JDK used to build the image). Modular JARs and directories are packaged as specified
by the `UniversalPlugin`.

.. code-block:: scala

  enablePlugins(JlinkPlugin)

The plugin requires Oracle JDK 11 or OpenJDK 11. Although `jlink` and `jdeps` are also
a part of the older JDK versions, those lack some of the newer features, which was not
addressed in the current plugin version.

This plugin must be run on the platform of the target installer. The tooling does *not*
provide a means of creating, say, Windows installers on MacOS, or MacOS on Linux, etc.

The plugin analyzes the dependencies between packages using `jdeps`, and raises an error in case of a missing dependency (e.g. for a provided transitive dependency). The missing dependencies can be suppressed on a case-by-case basis (e.g. if you are sure the missing dependency is properly handled):

.. code-block:: scala

    jlinkIgnoreMissingDependency := JlinkIgnore.only(
      "foo.bar" -> "bar.baz",
      "foo.bar" -> "bar.qux"
    )

For large projects with a lot of dependencies this can get unwieldy. You can use a more flexible ignore strategy:

.. code-block:: scala

  jlinkIgnoreMissingDependency := JlinkIgnore.byPackagePrefix(
    "foo.bar" -> "bar"
  )

Otherwise you may opt out of the check altogether (which is not recommended):

.. code-block:: scala

   jlinkIgnoreMissingDependency := JlinkIgnore.everything

Further reading
---------------

For further details on the capabilities of `jlink`, see the
`jlink <https://docs.oracle.com/en/java/javase/11/tools/jlink.html>`_ and
`jdeps <https://docs.oracle.com/en/java/javase/11/tools/jdeps.html>`_ references.
(Note: only some of the possible settings are exposed through this plugin. Please submit a
`Github <https://github.com/sbt/sbt-native-packager/issues>`_ issue or pull request if something specific is desired.)
