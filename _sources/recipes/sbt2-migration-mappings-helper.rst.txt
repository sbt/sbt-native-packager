.. _sbt2-migration-mappings-helper:

Updating MappingsHelper.directory/contentOf to SBT 2.x
=======================================================

SBT 2.x replaced ``File``-keyed mappings with virtual-file-keyed mappings (``HashedVirtualFileRef``).
Turning a plain ``java.io.File`` into such a reference requires the ``FileConverter`` provided by the
``fileConverter`` setting, and that setting is only available inside a task or setting body, i.e. wherever
you can call ``.value``.

``MappingsHelper.directory(String)`` and ``MappingsHelper.contentOf(String)`` are usually called directly
at the top level of a ``build.sbt`` file, outside of any task/setting body, which is exactly where
``fileConverter`` can't be resolved. To keep the DSL working, both methods now return a
``Def.Initialize[Seq[(FileRef, String)]]`` on SBT 2.x, which resolves the converter internally as soon as
you call ``.value`` on the result.

**SBT 1.x** (unchanged):

.. code-block:: scala

    Universal / mappings ++= directory("SomeDirectoryNameToInclude")

**SBT 2.x**: append ``.value``:

.. code-block:: scala

    Universal / mappings ++= directory("SomeDirectoryNameToInclude").value

The same applies to ``contentOf``:

.. code-block:: scala

    Universal / mappings ++= contentOf("SomeDirectoryNameToInclude").value


.. hint:: If your build cross-compiles the same ``build.sbt`` against both SBT 1.x and SBT 2.x (for example
   in a plugin's own scripted tests), be aware that ``directory(String)``/``contentOf(String)`` do not have
   the same return type on both versions, so a single source file cannot use ``.value`` unconditionally on
   both. Prefer the explicit ``File``-argument form shown above for code that has to build under both
   versions, and reserve the ``.value`` shorthand for build files that only ever run under SBT 2.x.
