Deployment
==========
This page shows you how to configure your build to deploy your build universal(zip, tgz, txz), rpm, debian or msi packages.
For information on docker, please take a look at the docker page.

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

Default Configuration
---------------------
The easiest way is to add ``deploymentSettings`` to your ``build.sbt``

.. code-block:: scala

    import NativePackagerKeys._

    packageArchetype.java_server

    deploymentSettings
    

You are now able to publish your application by scope.


  ``debian:publish``
    Publish jars along with the ``deb`` package

  ``rpm:publish``
    Publish jars along with the ``rpm`` package
    
  ``windows:publish``
    Publish jars along with the ``msi`` package

  ``universal:packageBin``
    Publish jars along with the ``zip`` package
    
  ``universal:packageZipTarball``
    Publish jars along with the ``tgz`` package
    
  ``universal:packageXzTarball``
    Publish jars along with the ``txz`` package

    


Custom Configuration
--------------------
You configure only what you need as well.


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

