.. _long-classpaths:

Dealing with long classpaths
============================

By default, when the native packager generates a script for starting your application, it will generate an invocation
of java that that passes every library on the classpath to the classpath argument, ``-cp``.  If you have a lot of
dependencies, this may result in a very long command being executed, which, aside from being aesthetically unpleasing
and difficult to work with when using tools like ``ps``, causes problems on some platforms, notably Windows, that have
limits to how long commands can be.

There are a few ways you can work around this in the native packager.

.. _launcher-jar-plugin:

Generate a launcher jar
-----------------------

The native packager includes a plugin that allows generating a launcher jar.  This launcher jar will contain no classes,
but will have your projects main class and classpath in its manifest.  The script that sbt then generates executes this
jar like so:

.. code-block:: bash

    java -jar myproject-launcher.jar

To enable the launcher jar, enable the ``LauncherJarPlugin``:

.. code-block:: scala

    enablePlugins(LauncherJarPlugin)

.. _classpath-jar-plugin:

Generate a classpath jar
------------------------

The classpath jar is very similar to the launcher jar, in that it also has the classpath on its manifest, but it does
not include the main class in its manifest, and so executed by the start script by invoking:

.. code-block:: bash

    java -cp myproject-classpath.jar some.Main

To enable the classpath jar:

.. code-block:: scala

    enablePlugins(ClasspathJarPlugin)

Configure a wildcard classpath
------------------------------

JDK 6 and above supports configuring the classpath using wildcards.  To enable this, simply override the
``scriptClasspath`` task to only contain ``*``, for example:

.. code-block:: scala

    scriptClasspath := Seq("*")

One downside of this approach is that the classpath ordering will no longer match the classpath ordering that sbt uses.
