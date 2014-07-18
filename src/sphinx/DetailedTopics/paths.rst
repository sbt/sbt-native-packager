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

========================================================  ===================  =====================  =======
output path                                               scopes               archetype              comment
========================================================  ===================  =====================  =======
lib                                                       all                  JavaApp
conf                                                      all                  JavaApp
bin/``<packageName>``                                     Global, Debian, Rpm  JavaApp
bin/``<packageName>``                                     Docker                                      Entrypoint DockerPlugin
bin/``<packageName>``.bat                                 Global               JavaApp
``<defaultLinuxInstallationLocation>``/``<packageName>``  Linux, Debian, Rpm   JavaApp
``<defaultLinuxLogLocation>``/``<packageName>``           Linux                JavaServerApplication
logs                                                      Linux                JavaServerApplication  Symlink
/etc/default/``<packageName>``                            Linux                JavaServerApplication
/var/run/``<packageName>``                                Linux                JavaServerApplication
/etc/init.d/``<packageName>``                             Linux, Debian, Rpm   JavaServerApplication  For SystemV
/etc/init/``<packageName>``                               Linux, Debian, Rpm   JavaServerApplication  For Upstart
/usr/lib/systemd/system/``<packageName>``.service         Linux, Debian, Rpm   JavaServerApplication  For Systemd
``<defaultLinuxInstallLocation>``                         Global, Docker                              Installation path inside the container
========================================================  ===================  =====================  =======

      




Settings
--------

Debian requires the following specific settings:

  ``name``
    Use for the normal jar generation process

  ``normalizedName``
    Use for the normal jar generation process.

  ``packageName``
    Defaults to ``normalizedName``. Can be override in different scopes

