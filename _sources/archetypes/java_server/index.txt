Java Server Application Archetype
#################################

In the :doc:`Application Packaging </archetypes/java_app/index>` section we described how to build and
customize settings related to an application. The server archetype adds additional features you may
need when running your application as a service on a server. SBT Native Packager ships with a set of
predefined install and uninstall scripts for various platforms and service managers.


Features
--------

The `JavaServerAppPackaging` archetype contains all `JavaAppPackaging` feature and the following

* install/uninstall services
* default mappings for server applications
* Creates a start script in ``/etc/init.d`` (SystemV) or ``/etc/init/`` (Upstart)


Usage
-----

.. raw:: html

  <div class="row">
    <div class="col-md-6">

Version 1.0 or higher with sbt 0.13.5 and and higher

.. code-block:: scala

  enablePlugins(JavaServerAppPackaging)

.. raw:: html

    </div><!-- v1.0 -->
    <div class="col-md-6">
    
Version 0.8 or lower

.. code-block:: scala

  import com.typesafe.sbt.SbtNativePackager._
  import NativePackagerKeys._
  
  packageArchetype.java_server

.. raw:: html

    </div><!-- v0.8 -->
  </div><!-- row end -->


Customize
---------

The server archetype provides :doc:`additional options to customize your application <customize>`
behaviour at buildtime, installation, uninstallation and during runtime. The
basic application script customization is discussed in :doc:`Java Application Customization </archetypes/java_app/customize>`.

Service Managers
----------------

Platforms are tied to both package managers (Rpm, Debian) and Service Managers (System V, Upstart, SystemD). By 
default the native packager will configure a service manager to run the daemon process. The available 
configurations are:

+---------------+--------------------+--------------+
| Platform      |  Service Manager   |  Working     |
+===============+====================+==============+
| Ubuntu        | Upstart (Default)  |    X         |
+---------------+--------------------+--------------+
| Ubuntu        | System V           |    X         |
+---------------+--------------------+--------------+
| CentOS        | System V (Default) |    X         |
+---------------+--------------------+--------------+
| CentOS 6.5    | Upstart            |    X         |
+---------------+--------------------+--------------+
| Fedora        | System V (Default) |    X         |
+---------------+--------------------+--------------+
| Fedora        | systemd            | experimental |
+---------------+--------------------+--------------+
| Windows       | Windows Services   |              |
+---------------+--------------------+--------------+

Sitemap
-------

.. toctree::
   :maxdepth: 1
   
   my-first-project.rst
   customize.rst


Next, let's :doc:`get started with simple application <my-first-project>`


