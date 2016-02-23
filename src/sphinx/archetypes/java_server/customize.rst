Customize Java Server Applications
##################################

Application Configuration
=========================

After :doc:`creating a package <my-first-project>`, the very next thing needed, usually, is the ability for users/ops to customize the application once it's deployed.   Let's add some configuration to the newly deployed application.

There are generally two types of configurations:

* Configuring the JVM and the process
* Configuring the Application itself.

The server archetype provides you with a special feature to configure your application
with a single file outside of customizing the ``bash`` or ``bat`` script for applications. 
As this file is OS dependent, each OS gets section.

Linux Configuration
-------------------

There are different ways described in :doc:`Customizing the Application </archetypes/java_app/customize>`
and can be used the same way.

The server archetype adds an additional way with an ``etc-default`` file placed
in ``src/templates``, which currently only works for **SystemV** and
**systemd**. The file gets sourced before the actual startscript is executed.
The file will be installed to ``/etc/default/<normalizedName>``

Example `/etc/default/<normalizedName>` for SystemV:

.. code-block :: bash

    # Available replacements 
    # ------------------------------------------------
    # ${{author}}           package author
    # ${{descr}}            package description
    # ${{exec}}             startup script name
    # ${{chdir}}            app directory
    # ${{retries}}          retries for startup
    # ${{retryTimeout}}     retry timeout
    # ${{app_name}}         normalized app name
    # ${{daemon_user}}      daemon user
    # -------------------------------------------------

    # Setting JAVA_OPTS
    # -----------------
    JAVA_OPTS="-Dpidfile.path=/var/run/${{app_name}}/play.pid $JAVA_OPTS"
    
    # For rpm/systemv you need to set the PIDFILE env variable as well
    PIDFILE="/var/run/${{app_name}}/play.pid"
    
    # export env vars for 3rd party libs
    # ----------------------------------
    COMPANY_API_KEY=123abc
    export COMPANY_API_KEY


Environment variables
~~~~~~~~~~~~~~~~~~~~~

The usual ``JAVA_OPTS`` can be used to override settings. This is a nice way to test
different jvm settings with just restarting the jvm.

Windows Configuration
---------------------

Support planned.


Service Manager Configuration
=============================

It is possible to change the default Service Manager for a given platform by specifying a ``ServerLoader``. To use 
Upstart for an Rpm package simply:

.. code-block:: scala

    import com.typesafe.sbt.packager.archetypes.ServerLoader
    
    serverLoading in Rpm := ServerLoader.Upstart


*As a side note Fedora/RHEL/Centos family of linux specifies* ``Default requiretty`` *in its* ``/etc/sudoers`` 
*file. This prevents the default Upstart script from working correctly as it uses sudo to run the application
as the* ``daemonUser`` *. Simply disable requiretty to use Upstart or modify the Upstart template.* 

Customize Start Script
----------------------

Sbt Native Packager leverages templating to customize various start/stop scripts and pre/post install tasks. 
As an example, to alter the ``loader-functions`` which manage the specific start and stop process commands 
for SystemLoaders you can to the ``linuxScriptReplacements`` map:

.. code-block:: scala

  import com.typesafe.sbt.packager.archetypes.TemplateWriter

  linuxScriptReplacements += {
    val functions = sourceDirectory.value / "templates" / "custom-loader-functions"
    // Nil == replacements. If you want to replace stuff in your script put them in this Seq[(String,String)]
    "loader-functions" -> TemplateWriter.generateScript(functions.toURL, Nil)
  }

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

The :doc:`debian </formats/debian>` and :doc:`redhat </formats/rpm>` pages have further information on overriding 
distribution specific actions.

Override Start Script
-----------------------------------------------

It's also possible to override the entire script/configuration for your service manager.
Create a file ``src/templates/$format/$loader`` and it will be used instead.

Possible values:

* ``$format`` - ``debian`` or ``rpm``
* ``$loader`` - ``upstart``, ``systemv`` or ``systemd``

**Syntax**

You can use ``${{variable_name}}`` to reference variables when writing your script.  The default set of variables is:

* ``descr`` - The description of the server.
* ``author`` - The configured author name.
* ``exec`` - The script/binary to execute when starting the server
* ``chdir`` - The working directory for the server.
* ``retries`` - The number of times to retry starting the server.
* ``retryTimeout`` - The amount of time to wait before trying to run the server.
* ``app_name`` - The name of the application (linux friendly)
* ``app_main_class`` - The main class / entry point of the application.
* ``app_classpath`` - The (ordered) classpath of the application.
* ``daemon_user`` - The user that the server should run as.


SystemD Support
---------------

There is also experimental SystemD support for Fedora release 20 (Heisenbug). You can use the ```Systemd``` server loader:

.. code-block:: scala

   import com.typesafe.sbt.packager.archetypes.ServerLoader

   serverLoading in Rpm := ServerLoader.Systemd

There is only partial systemd support in Ubuntu 14.04 LTS which prevents sbt-native-packager systemd from working correctly on
Ubuntu. Ubuntu 15.04 is the first version that switched to Systemd and the default Upstart won't work. Switch to Systemd with

.. code-block:: scala

   import com.typesafe.sbt.packager.archetypes.ServerLoader

   serverLoading in Debian := ServerLoader.Systemd

Package Lifecycle Configuration
===============================

Some scripts are covered in the standard application type. Read more on :doc:`Java Application Customization</archetypes/java_app/customize>`.
For the ``java_server`` package lifecycle scripts are customized to provide the following additional features

* Chowning directories and files correctly (if necessary)
* Create/Delete users and groups according to your mapping
* Register application at your init system

For this purpose *sbt-native-packager* ships with some predefined templates. These can be
overridden with different techniques, depending on the packaging system.

Partially Replace Template Functionality
----------------------------------------

Most sbt-native-packager scripts are broken up into partial templates in the `resources directory 
<https://github.com/sbt/sbt-native-packager/tree/master/src/main/resources/com/typesafe/sbt/packager>`_. 
You can override these default template snippets by adding to the ``linuxScriptReplacements`` map. As
an example you can change the ``loader-functions`` which starts/stop services based on a certain ```ServerLoader```:

.. code-block:: scala

  linuxScriptReplacements += "loader-functions" -> TemplateWriter.generateScript(getClass.getResource("/custom-loader-functions"), Nil)

The ``custom-loader-functions`` file must declare the ``startService()`` and ``stopService()`` functions used in various
service management scripts.


RPM Scriptlets
--------------

RPM puts all scripts into one file. To override or append settings to your
scriptlets use these settings:
     
   ``rpmPre`` 
     %pre scriptlet
   
   ``rpmPost`` 
     %post scriptlet
   
   ``rpmPosttrans`` 
     %posttrans scriptlet
     
   ``rpmPreun`` 
     "%preun scriptlet"
     
   ``rpmPostun`` 
     %postun scriptlet
     
   ``rpmVerifyscript`` 
     %verifyscript scriptlet

If you want to have your files separated from the build definition use the
default location for rpm scriptlets. To override default templates in a RPM
build put the new scriptlets in the ``rpmScriptsDirectory`` (by default ``src/rpm/scriptlets``). 

   ``rpmScriptsDirectory`` 
     By default to ``src/rpm/scriptlets``. Place your templates here.    
    
Available templates are

    ``post-rpm``
    ``pre-rpm``
    ``postun-rpm``
    ``preun-rpm``
    
Override Postinst scriptlet
~~~~~~~~~~~~~~~~~~~~~~~~~~~

By default the ``post-rpm`` template only starts the service, but doesn't register it.

.. code-block :: bash

    service ${{app_name}} start
    
For **CentOS** we can do 

.. code-block :: bash

    chkconfig ${{app_name}} defaults
    service ${{app_name}} start || echo "${{app_name}} could not be started. Try manually with service ${{app_name}} start"
    
For **RHEL**

.. code-block :: bash

    update-rc.d ${{app_name}} defaults
    service ${{app_name}} start || echo "${{app_name}} could not be started. Try manually with service ${{app_name}} start"

    

Debian Control Scripts
----------------------

To override default templates in a Debian build put the new control files in the
``debianControlScriptsDirectory`` (by default ``src/debian/DEBIAN``). 

   ``debianControlScriptsDirectory`` 
     By default to ``src/debian/DEBIAN``. Place your templates here.
    
   ``debianMakePreinstScript``
     creates or discovers the preinst script used by this project.
    
   ``debianMakePrermScript``
     creates or discovers the prerm script used by this project.

   ``debianMakePostinstScript``
     creates or discovers the postinst script used by this project.

   ``debianMakePostrmScript``
     creates or discovers the postrm script used by this project.

    
Available templates are

   ``postinst``
   ``preinst``
   ``postun``
   ``preun``
 
 
Linux Replacements
------------------
 
 This is a list of values you can access in your templates
 
 .. code-block :: bash
 
      ${{author}}
      ${{descr}}
      ${{exec}}
      ${{chdir}}
      ${{retries}}
      ${{retryTimeout}}
      ${{app_name}}
      ${{daemon_user}}
      ${{daemon_group}}
 

Example Configurations
======================

A list of very small configuration settings can be found at `sbt-native-packager-examples`_

    .. _sbt-native-packager-examples: https://github.com/muuki88/sbt-native-packager-examples

