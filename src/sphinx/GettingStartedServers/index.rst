Getting Started with Servers
############################

The sbt-native-packager is an sbt plugin for bundling your server for a variety of platforms.  

**Note:** Please follow the :ref:`Installation` instructions for how to set it up on a project.

In the :doc:`Application Packaging <GettingStartedApplications>` section we described how to build and
customize settings related to the application. Sbt-Native-Packager provides a further level for servers
which define how applications are installed and initialized for various platforms. 
be customized for specific platforms. While it provides
some basic abstractions around packaging, it also allows you to dig down into the nuts and bolts of each platform as
neeeded to generate the best package possible.   

Service Managers
================

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

Changing Service Managers
=========================

It is possible to change the default Service Manager for a given platform by specifying a ``ServerLoader``. To use 
Upstart for an Rpm package simply:

.. code-block:: scala

    serverLoading in Rpm := ServerLoader.Upstart


*As a side note Fedora/RHEL/Centos family of linux          
specifies* ``Default requiretty`` *in its* ``/etc/sudoers`` 
*file. This prevents the default Upstart script from working 
correctly as it uses sudo to run the application            
as the* ``daemonUser`` *. Simply disable requiretty          
to use Upstart or modify the Upstart template.*             

Sbt Native Packager leverages templating to customize various start/stop scripts and pre/post install tasks. 
The :doc:`templating reference <OverridingTemplates>` describes this functionality in-depth.
As an example, to alter the ``loader-functions`` which manage the specific start and stop process commands 
for SystemLoaders you can to the ``linuxScriptReplacements`` map:

.. code-block:: scala

  linuxScriptReplacements += "loader-functions" -> TemplateWriter.generateScript(getClass.getResource("/custom-loader-functions"), Nil)

which will add the following resource file to use start/stop instead of initctl in the post install script:

.. code-block:: bash

  startService() {
      app_name=$1
      start $app_name 
  }

  stopService() {
      app_name=$1
      stop $app_name 
  }

The :doc:`debian <DetailedTopics/debian>` and :doc:`redhat </DetailedTopics/redhat>` pages have further information on overriding distribution scpecific actions.

SystemD Support
================

There is also experimental systemd support for Fedora release 20 (Heisenbug). You can use the ```Systemd``` server loader:

.. code-block:: scala

   serverLoading in Rpm:= ServerLoader.Systemd

There is only partial systemd support in Ubuntu 14.04 LTS which prevents sbt-native-packager systemd from working correctly on
Ubuntu.

What is a Server Archetype
==========================

A server project extends the basic ``java_application`` with some server specific features,
which are currently on

Linux
~~~~~

* ``/var/log/<pkg>`` is symlinked from ``<install-location>/log``

* ``/var/run/<pkg>`` is created with write privileges for the ``daemonUser``
  
* ``/etc/<pkg>`` is symlinked from ``<install-location>/conf``

* Creates a start script in ``/etc/init.d`` (SystemV) or ``/etc/init/`` (Upstart)

* Creates a startup config file in ``/etc/default/<pkg>``


Next, let's :doc:`get started with simple application <MyFirstProject>`


