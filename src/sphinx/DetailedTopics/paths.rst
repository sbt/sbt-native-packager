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
    

