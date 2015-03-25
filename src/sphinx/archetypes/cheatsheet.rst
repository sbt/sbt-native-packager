.. _Cheatsheet:

Archetype Cheatsheet
####################

This is a set FAQ composed on a single page.

Path Configurations
===================
This section describes where and how to configure different kind of paths settings.

- What is the installation location of my package
- Where is the log directory created
- What is the name of my start script
- ...



Quick Reference Table
---------------------
This table gives you a quick overview of the setting and the scope you should use.
Paths which do not begin with a ``/`` are relative to the universal directory.
The scopes are ordered from general to specific, so a more specific one will override
the generic one. Only the listed scopes for a setting a relevant. Any changes in other
scopes will have no effect!

========================================================  ===================  =====================  =======
output path                                               scopes               archetype              comment
========================================================  ===================  =====================  =======
lib                                                       all                  JavaApp
conf                                                      all                  JavaApp
bin/``<executableScriptName>``                            Global               JavaApp
bin/``<executableScriptName>``.bat                        Global               JavaApp
bin/``<executableScriptName>``                            Global                                      Entrypoint DockerPlugin
``<defaultLinuxInstallationLocation>``/``<packageName>``  Linux, Debian, Rpm   JavaApp
``<defaultLinuxLogLocation>``/``<packageName>``           Linux                JavaServerApplication
logs                                                      Linux                JavaServerApplication  Symlink
/etc/default/``<packageName>``                            Linux                JavaServerApplication
/var/run/``<packageName>``                                Linux                JavaServerApplication
/etc/init.d/``<packageName>``                             Linux, Debian, Rpm   JavaServerApplication  For SystemV
/etc/init/``<packageName>``                               Linux, Debian, Rpm   JavaServerApplication  For Upstart
/usr/lib/systemd/system/``<packageName>``.service         Linux, Debian, Rpm   JavaServerApplication  For Systemd
``<defaultLinuxInstallLocation>``                         Docker                                      Installation path inside the container
========================================================  ===================  =====================  =======


Settings
--------

These settings configure the path behaviour

  ``name``
    Use for the normal jar generation process

  ``packageName``
    Defaults to ``normalizedName``. Can be override in different scopes
    
  ``executableScriptName``
    Defaults to ``normalizedName``. Sets the name of the executable starter script

  ``defaultLinuxInstallationLocation``
    Defaults to ``/usr/share/``. Used to determine the installation path for for linux packages (rpm, debian)
    
  ``defaultLinuxLogLocation``
    Defaults to ``/var/log/``. Used to determine the log path for linux packages (rpm, debian).
    


Overriding Templates
====================

You can override the default template used to generate any of the scripts in
any archetype.   Listed below are the overridable files and variables that
you can use when generating scripts.

Bat Script - ``src/templates/bat-template``
-------------------------------------------

Creating a file here will override the default template used to
generate the ``.bat`` script for windows distributions.

**Syntax**

``@@APP_ENV_NAME@@`` - will be replaced with the script friendly name of your package.

``@@APP_NAME@@`` - will be replaced with user friendly name of your package.

``@APP_DEFINES@@`` - will be replaced with a set of variable definitions, like
  ``APP_MAIN_CLASS``, ``APP_MAIN_CLASS``.

You can define additional variable definitions using ``batScriptExtraDefines``.

Bash Script - ``src/templates/bash-template``
---------------------------------------------

Creating a file here will override the default template used to
generate the BASH start script found in ``bin/<application>`` in the
universal distribution

**Syntax**

``${{template_declares}}`` - Will be replaced with a series of ``declare <var>``
lines based on the ``bashScriptDefines`` key.  You can add more defines to
the ``bashScriptExtraDefines`` that will be used in addition to the default set:

* ``app_mainclass`` - The main class entry point for the application.
* ``app_classpath`` - The complete classpath for the application (in order).



Service Manager - ``src/templates/start``
-----------------------------------------

Creating a file here will override either the init.d startup script or
the upstart start script.  It will either be located at
``/etc/init/<application>`` or ``/etc/init.d/<application>`` depending on which
serverLoader is being used.

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

Server App Config - ``src/templates/etc-default``
-------------------------------------------------

Creating a file here will override the ``/etc/default/<application>`` template
used when SystemV is the server loader.

