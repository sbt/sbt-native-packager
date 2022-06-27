Embedding JVM in Universal
==========================

Sbt Native Packager supports embedding the jvm using the :ref:`jdkpackager-plugin`,
however, in some cases you may want instead to embed the JVM/JRE in other formats,
e.g. a tarball with one of the java archetypes.

To accomplish this you need to:

* Add the JVM/JRE of your choice to the `mappings`
* Make the launcher use the embedded `jre`

Adding the JVM
--------------

The JRE is by definition OS dependent, hence you must choose the one appropriate
for your case. The example below assumes a Linux 64 JRE, whose files are at
``$HOME/.jre/linux64``. The files will be copied to a ``jre`` directory in your
distribution

.. code-block:: scala

      import NativePackagerHelper._

      ...

      Universal / mappings ++= {
        val jresDir = Path.userHome / ".jre"
        val linux64Jre = jresDir.toPath.resolve("linux64")
        directory(linux64Jre.toFile).map { j =>
          j._1 -> j._2.replace(jreLink, "jre")
        }
      }

Application Configuration
-------------------------

In order to run your application in production you also need to make the launcher
use the jre added above. This can be done using the ``-java-home`` option with a
relative path.

.. code-block:: scala

  Universal / javaOptions ++= Seq(
    // Your original options

   "-java-home ${app_home}/../jre"
  )
