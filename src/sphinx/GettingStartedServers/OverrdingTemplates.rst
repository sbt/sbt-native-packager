Overriding templates
####################

Some scripts are covered in the standard application type. Read more on :doc:`../GettingStartedApplications/OverridingTemplates`.
For the ``java_server`` package lifecycle scripts are customized to provide the following additional features

* Chowning directories and files correctly
* Create/Delete users and groups according to your mapping
* Register application at your init system

For this purpose *sbt-native-packager* ships with some predefined templates. These can be
override with different techniques, depending on the packaging system.

RPM Scriptlets
==============

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
    
Override Postinst scriplet
~~~~~~~~~~~~~~~~~~~~~~~~~~

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
======================

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
==================
 
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
 