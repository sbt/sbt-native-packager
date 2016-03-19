.. _Cheatsheet:

Archetype Cheatsheet
####################

This is a set FAQ composed on a single page.

Path Configurations
===================
This section describes where and how to configure different kind of paths settings like

- what is the installation location of my package
- where is the log directory created
- what is the name of my start script



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

  ``defaultLinuxInstallLocation``
    Defaults to ``/usr/share/``. Used to determine the installation path for for linux packages (rpm, debian)

  ``defaultLinuxLogsLocation``
    Defaults to ``/var/log/``. Used to determine the log path for linux packages (rpm, debian).


JVM Options
===========

JVM options can be added via different mechanisms. It depends on your use case which is most suitable.
The available options are

- Adding via ``bashScriptExtraDefines`` and ``batScriptExtraDefines``
- Providing a ``application.ini`` (JavaApp) or ``etc-default`` (JavaServer) file
- Set ``javaOptions in Universal`` (JavaApp) or ``javaOptions in Linux`` (JavaServer, linux only)

.. warning:: If you want to change the location of your config keep in mind that the path in
    **bashScriptConfigLocation** should either
    - be **absolute** (e.g. */etc/etc-default/my-config<*) or
    - starting with *${app_home}/../* (e.g. *${app_home}/../conf/application.ini*)

Extra Defines
-------------

With this approach you are altering the bash/bat script that gets executed.
Your configuration is literally woven into it, so it applies to any archetype
using this bashscript (app, akka app, server, ...).

For a bash script this could look like this.

.. code-block:: scala

     bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/app.config""""

     // or more. -X options don't need to be prefixed with -J
     bashScriptExtraDefines ++= Seq(
        """addJava "-Xms1024m"""",
        """addJava "-Xmx2048m""""
     )

For information take a look at the :doc:` customize section for java apps </archetypes/java_app/customize>`

File - application.ini or etc-default
-------------------------------------

Another approach would be to provide a file that is read by the bash script during execution.

Java App
~~~~~~~~

Create a file ``src/universal/conf/application.ini`` (gets automatically added to the package mappings)
and add this to your ``build.sbt`` inject the config location into the bashscript.

.. code-block:: scala

    bashScriptConfigLocation := Some("${app_home}/../conf/application.ini")


Java Server
~~~~~~~~~~~

See :ref:`server-app-config`

Setting - javaOptions
---------------------

The last option to set your java options is using ``javaOptions in Universal`` (JavaApp and Server).
This will generate files according to your archetype. The following table gives you an overview what
you can use and how things will be behave if you mix different options. Options lower in the table
are more specific and will thus override the any previous settings (if allowed).

========  =========  ========================  ==========  ========  =======
javaOpts  Scope      bashScriptConfigLocation  Archetype   mappings  comment
========  =========  ========================  ==========  ========  =======
Nil       Universal  None                      JavaApp               No jvm options
Nil       Universal  Some(appIniLocation)      JavaApp               User provides the application.ini file in ``src/universal/conf/application.ini``
opts      Universal  Some(_)                   JavaApp     added     creates ``application.ini`` but leaves ``bashScriptConfigLocation`` unchanged
opts      Universal  None                      JavaApp     added     creates ``application.ini`` and sets ``bashScriptConfigLocation``. If ``src/universal/conf/application.ini`` is present it will be overridden
Nil       Linux      None                      JavaServer  added     creates ``etc-default`` and sets ``bashScriptConfigLocation``
opts      Linux      None                      JavaServer  added     creates ``etc-default``, appends ``javaOptions in Linux`` and sets ``bashScriptConfigLocation``
opts      Linux      Some(_)                   JavaServer  added     creates ``etc-default``, appends ``javaOptions in Linux`` and overrides ``bashScriptConfigLocation``
========  =========  ========================  ==========  ========  =======



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



Service Manager
-----------------------------------------

It's also possible to override the entire script/configuration for your service manager.
Create a file ``src/templates/$format/$loader`` and it will be used instead.

Possible values:

* ``$format`` - ``debian`` or ``rpm``
* ``$loader`` - ``upstart``, ``systemv`` or ``systemd``

**Syntax**

You can use ``${{variable_name}}`` to reference variables when writing your script.  The default set of variables is:

* ``author`` - The name of the author; defined by ``maintainer in Linux``.
* ``descr`` - The short description of the service; defined by ``packageSummary in Linux``.
* ``exec`` - The script/binary to execute when starting the service; defined by ``executableScriptName in Linux``.
* ``chdir`` - The working directory for the service; defined by ``defaultLinuxInstallLocation/(packageName in Linux)``.
* ``retries`` - The number of times to retry starting the server; defined to be the constant ``0``.
* ``retryTimeout`` - The amount of time to wait before trying to run the server; defined to be the constant ``60``.
* ``app_name`` - The name of the application (linux friendly); defined by ``packageName in Linux``.
* ``version`` - The software version; defined by ``version``.
* ``daemon_user`` - The user that the service should run as; defined by ``daemonUser in Linux``.
* ``daemon_user_uid`` - The user ID of the user that the service should run as; defined by ``daemonUserUid in Linux``.
* ``daemon_group`` - The group of the user that the service should run as; defined by ``daemonGroup in Linux``.
* ``daemon_group_gid`` - The group ID of the group of the user that the service should run as; defined by ``daemonGroupGid in Linux``.
* ``daemon_shell`` - The shell of the user that the service should run as; defined by ``daemonShell in Linux``.
* ``term_timeout`` - The timeout for the service to respond to a TERM signal; defined by ``termTimeout in Linux``, defaults to ``60``.
* ``kill_timeout`` - The timeout for the service to respond to a KILL signal; defined by ``killTimeout in Linux``, defaults to ``30``.
* ``start_facilities`` - Intended for the ``Required-Start:`` line in the ``INIT INFO`` block. Its value is automatically generated with respect to the chosen system loader.
* ``stop_facilities`` - Intended for the ``Required-Stop:`` line in the ``INIT INFO`` block. Its value is automatically generated with respect to the chosen system loader.
* ``start_runlevels`` - Intended for the ``Default-Start:`` line in the ``INIT INFO`` block. Its value is automatically generated with respect to the chosen system loader.
* ``stop_runlevels`` - Intended for the ``Default-Stop:`` line in the ``INIT INFO`` block. Its value is automatically generated with respect to the chosen system loader.

.. _server-app-config:

Server App Config - ``src/templates/etc-default-{systemv,systemd}``
-------------------------------------------------------------------

Creating a file here will override the ``/etc/default/<application>`` template
for the corresponding loader.

The file `/etc/default/<application>` is used as follows given the loader:

- *systemv*: sourced as a bourne script.
- *systemd*: used as an EnvironmentFile directive parameter (see *man systemd.exec*, section *EnvironmentFile* for a
  description of the expected file format).
- *upstart*: presently ignored.

If you're only overriding `JAVA_OPTS`, your environment file could be compatible
with both systemv and systemd loaders; if such is the case, you can specify a
single file at `src/templates/etc-default` which will serve as an override for
all loaders.
