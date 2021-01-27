.. _java-server-customize:


Application Configuration
=========================

After :doc:`creating a package <java_app/index.rst>`, the very next thing needed, usually, is the ability for users/ops to customize the application once it's deployed.   Let's add some configuration to the newly deployed application.

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

Daemon User and Group
~~~~~~~~~~~~~~~~~~~~~

Customize the daemon user and group for your application with the following settings.

.. code-block:: scala

    // a different daemon user
    daemonUser in Linux := "my-user"
    // if there is an existing one you can specify the uid
    daemonUserUid in Linux := Some("123")
    // a different daemon group
    daemonGroup in Linux := "my-group"
    // if the group already exists you can specify the uid
    daemonGroupGid in Linux := Some("1001")

Environment variables
~~~~~~~~~~~~~~~~~~~~~

The usual ``JAVA_OPTS`` can be used to override settings. This is a nice way to test
different jvm settings with just restarting the jvm.

Windows Configuration
---------------------

Support planned.


Systemloader Configuration
==========================

See the :ref:`systemloaders` documentation on how to add a systemloader (e.g. SystemV, Systemd or Upstart) to your
package.

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
scriptlets use ``maintainerScripts in Rpm`` or these ``RpmConstants._``s:

   ``Pre``
     %pre scriptlet

   ``Post``
     %post scriptlet

   ``Pretrans``
     %pretrans scriptlet
     
   ``Posttrans``
     %posttrans scriptlet

   ``Preun``
     "%preun scriptlet"

   ``Postun``
     %postun scriptlet

   ``Verifyscript``
     %verifyscript scriptlet

If you want to have your files separated from the build definition use the
default location for rpm scriptlets. To override default templates in a RPM
build put the new scriptlets in the ``rpmScriptletsDirectory`` (by default ``src/rpm/scriptlets``).

   ``RpmConstants.Scriptlets``
     By default to ``src/rpm/scriptlets``. Place your templates here.

Available templates are

    ``post-rpm``
    ``pre-rpm``
    ``postun-rpm``
    ``preun-rpm``
    
The corresponding maintainer file names are: 

    ``pretrans``
    ``post``
    ``pre``
    ``postun``
    ``preun``
    ``verifyscript``
    ``posttrans``

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

.. attention::
    Every replacement corresponds to a single setting or task. For the `linuxScriptReplacements` you need
    to override the setting/task in the `in Linux` scope. For example

    ``daemonUser in Linux := "new-user"``

    overrides the ``daemon_user`` in the linuxScriptReplacements.

Example Configurations
======================

A list of very small configuration settings can be found at `sbt-native-packager-examples`_

    .. _sbt-native-packager-examples: https://github.com/muuki88/sbt-native-packager-examples
