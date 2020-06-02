Deployment
==========
This page shows you how to configure your build to deploy your build universal(zip, tgz, txz), rpm, debian or msi
packages. For information on docker, please take a look at the docker page.

.. note:: The deployment settings only add artifacts to your publish task. Native packager doesn't provide any
    functionality for publishing to native repositories.

Setup publish Task
------------------

You need a working ``publish`` task in order to use the following configurations.
A good starting point is the `sbt publish documentation`_. You should have something
like this in your ``build.sbt``

.. code-block:: scala

    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (version.value.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    }

For an automatised build process are other plugins like the `sbt release plugin`_.

.. _sbt publish documentation: http://www.scala-sbt.org/0.13/docs/Publishing.html
.. _sbt release plugin: https://github.com/sbt/sbt-release

Default Deployment
------------------
The easiest way is to add ``UniversalDeployPlugin`` to your ``build.sbt``

.. code-block:: scala

    enablePlugins(JavaServerAppPackaging, UniversalDeployPlugin)

You are now able to publish your packaged application in both ``tgz`` and ``zip`` formats with:

  ``universal:publish``
    Publish the ``zip`` (or ``tgz``/``txz`` depending on the configuration. Default is to publish ``zip`` along with ``tgz``) package

Custom Deployments
------------------
When using other package formats we need to explicitly configure the
deployment setup to a more specific one.

RPM
~~~

Your ``build.sbt`` should contain:

.. code-block:: scala

    enablePlugins(RpmPlugin, RpmDeployPlugin)

This will make possible to push the ``RPM`` with:

  ``sbt rpm:publish``

Debian
~~~~~~

Enabled with:

.. code-block:: scala

    enable(DebianPlugin, DebianDeployPlugin)

that will make possible to publish a ``deb`` package with:

  ``sbt deb:publish``

Windows
~~~~~~~

If using an ``msi`` packaging you need to enable:

.. code-block:: scala

    enable(WindowsPlugin, WindowsDeployPlugin)

Then, pushing the package is

  ``sbt windows:publish``

Custom Configurations
---------------------
You could configure only what you need as well.


Debian
~~~~~~

.. code-block:: scala

    makeDeploymentSettings(Debian, packageBin in Debian, "deb")

    //if you want a changes file as well
    makeDeploymentSettings(Debian, genChanges in Debian, "changes")

RPM
~~~

.. code-block:: scala

    makeDeploymentSettings(Rpm, packageBin in Rpm, "rpm")

Windows
~~~~~~~

.. code-block:: scala

    makeDeploymentSettings(Windows, packageBin in Windows, "msi")

Universal
~~~~~~~~~

.. code-block:: scala

    // zip
    makeDeploymentSettings(Universal, packageBin in Universal, "zip")

    makeDeploymentSettings(UniversalDocs, packageBin in UniversalDocs, "zip")

    // additional tgz
    addPackage(Universal, packageZipTarball in Universal, "tgz")

    // additional txz
    addPackage(UniversalDocs, packageXzTarball in UniversalDocs, "txz")
