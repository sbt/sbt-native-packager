Java Application Archetype
##########################

Application packaging focuses on how your application is launched (via a ``bash`` or ``bat`` script), how dependencies
are managed and how configuration and other auxillary files are included in the final distributable. The `JavaAppPackaging` archetype
provides a default application structure and executable scripts to launch your application. 

Additionally there is :doc:`Server Packaging </archetypes/java_server/index>` which provides platform-specific
functionality for installing your application in server environments. You can customize specific debian and rpm packaging
for a variety of platforms and init service loaders including Upstart, System V and SystemD (experimental).

Features
--------

The `JavaAppPackaging` archetype contains the following features.

* Default application mappings (no fat jar)
* Executable bash/bat script


Usage
-----

.. raw:: html

  <div class="row">
    <div class="col-md-6">

Version 1.0 or higher with sbt 0.13.5 and and higher

.. code-block:: scala

  enablePlugins(JavaAppPackaging)

.. raw:: html

    </div><!-- v1.0 -->
    <div class="col-md-6">
    
Version 0.8 or lower

.. code-block:: scala

  import com.typesafe.sbt.SbtNativePackager._
  import NativePackagerKeys._
  
  packageArchetype.java_app

.. raw:: html

    </div><!-- v0.8 -->
  </div><!-- row end -->


Customize
---------

You can customize the bash/bat scripts in different ways. This is explained in
the :doc:`Customize <customize>` section. The application structure is customizable
via the standard mappings, which is described in the :doc:`Universal Plugin Section </formats/universal>`.


Sitemap
-------

.. toctree::
   :maxdepth: 1
   
   gettingstarted
   my-first-project.rst
   customize.rst
   generating-files.rst
   writing-documentation.rst
