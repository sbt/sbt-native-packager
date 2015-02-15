.. _GettingStarted:

.. contents:: 
  :depth: 2

Installation
============

The sbt-native-packager is a plugin. To use it, first create a ``project/plugins.sbt`` file with the following content. 

.. code-block:: scala

  addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "x.y.z")


Also, each operating system requires its own tools. These tools are specified
in the operating system specific sections.

Version 1.0 and greater
-----------------------

If you use sbt 0.13.5 or greater you can enable sbt native packager by enabling it in your ``build.sbt``.
We recommend to `use an archetype <archetypes/>`_ for setting up your build

.. code-block:: scala

  enablePlugins(JavaAppPackaging)

but if you only want the bare minimum you can only add the packager plugin

.. code-block:: scala

  enablePlugins(SbtNativePackager)
  
The autoplugins mechanism will import everything automatically.

Build.scala
~~~~~~~~~~~

If you use a ``Build.scala`` you can import the available keys
with this statement

.. code-block:: scala

  import com.typesafe.sbt.SbtNativePackager.autoImport._


Version 0.8.x or lower
----------------------


If you don't use autoplugins you need to import the available
keys yourself. In your ``build.sbt`` or ``Build.scala`` add

.. code-block:: scala

  import com.typesafe.sbt.SbtNativePackager._
  import NativePackagerKeys._



Packaging Formats
=================


.. raw:: html

  <hr>
  <div class="row">
      <div class="col-lg-3">
        <h2>*.deb</h2>
        <p>Packaging format for Debian based systems like Ubuntu</p>
        <pre>debian:packageBin</pre>
        <a class="btn btn-primary btn-lg" href="formats/debian.html" role="button"><i class="fa fa-linux"></i> Debian Plugin »</a>
      </div>
      <div class="col-lg-3">
        <h2>*.rpm</h2>
        <p>Packaging format for Redhat based systems like RHEL or CentOS.</p>
        <pre>rpm:packageBin</pre>
        <a class="btn btn-primary btn-lg" href="formats/rpm.html" role="button"><i class="fa fa-linux"></i> Rpm Plugin »</a>
      </div>
      <div class="col-lg-3">
        <h2>*.msi</h2>
        <p>Packaging format for windows systems.</p>
        <pre>windows:packageBin</pre>
        <a class="btn btn-primary btn-lg" href="formats/windows.html" role="button"><i class="fa fa-windows"></i> Windows Plugin »</a>
      </div>
      <div class="col-lg-3">
        <h2>*.dmg</h2>
        <p>Packaging format for osx based systems.</p>
        <pre>universal:packageOsxDmg</pre>
        <a class="btn btn-primary btn-lg" href="formats/universal.html" role="button"><i class="fa fa-apple"></i> Universal Plugin »</a>
      </div>
    </div>
    <div class="row">
      <div class="col-lg-3">
        <h2>docker</h2>
        <p>Package your application in a docker container.</p>
        <pre>docker:publishLocal</pre>
        <a class="btn btn-primary btn-lg" href="formats/docker.html" role="button"><i class="fa fa-file-archive-o"></i> Docker Plugin »</a>
      </div>
      <div class="col-lg-3">
        <h2>*.zip</h2>
        <p>Packaging format for all systems supporting zip.</p>
        <pre>universal:packageBin</pre>
        <a class="btn btn-primary btn-lg" href="formats/universal.html" role="button"><i class="fa fa-file-archive-o"></i> Universal Plugin »</a>
      </div>
      <div class="col-lg-3">
        <h2>*.tar</h2>
        <p>Packaging format for all systems supporting tar.</p>
        <pre>universal:packageZipTarball</pre>
        <a class="btn btn-primary btn-lg" href="formats/universal.html" role="button"><i class="fa fa-file-archive-o"></i> Universal Plugin »</a>
      </div>
      <div class="col-lg-3">
        <h2>*.xz</h2>
        <p>Packaging format for all systems supporting xz.</p>
        <pre>universal:packageXzTarball</pre>
        <a class="btn btn-primary btn-lg" href="formats/universal.html" role="button"><i class="fafa-file-archive-o"></i> Universal Plugin »</a>
      </div>
    </div>
    
    <link href="//maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css" rel="stylesheet">

Archetypes
==========


.. raw:: html

  <hr>
  <div class="row">
      <div class="col-lg-4">
        <h2>Java Application</h2>
        <p>Creates a standalone package with an executable bash/bat script.<br>&nbsp; </p>
        <pre>enablePlugins(JavaAppPackaging)</pre>
        <a class="btn btn-primary btn-lg" href="archetypes/java_app/" role="button"><i class="fa fa-play-circle-o"></i> Learn more »</a>
      </div>
      <div class="col-lg-4">
        <h2>Java Server</h2>
        <p>Creates a standalone package with an executable bash/bat script and additional configuration and autostart.</p>
        <pre>enablePlugins(JavaServerAppPackaging)</pre>
        <a class="btn btn-primary btn-lg" href="archetypes/java_server/" role="button"><i class="fa fa-gears"></i> Learn more »</a>
      </div>
      <div class="col-lg-4">
        <h2>Akka Microkernel</h2>
        <p>Like a the Java Application archetype, but instantiates and runs a subclass of 
        <a href="https://github.com/akka/akka/blob/master/akka-kernel/src/main/scala/akka/kernel/Main.scala">Bootable</a><br>&nbsp;</p>
        <pre>enablePlugins(AkkaAppPackaging)</pre>
        <a class="btn btn-primary btn-lg" href="archetypes/akka_app/" role="button"><i class="fa fa-cubes"></i> Learn more »</a>
      </div>
    </div>
    
    
    
Sitemap
=======

.. toctree::
   :maxdepth: 2
   
   archetypes/index.rst
   formats/index.rst
   topics/index.rst
