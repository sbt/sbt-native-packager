Rpm Plugin
==========

RedHat ``rpm`` files support a very advanced number of features.  To take full advantage of this environment,
it's best to understand how the ``rpm`` package system works.
http://fedoraproject.org/wiki/How_to_create_an_RPM_package is a good tutorial, but it focuses on building
packages from source.   The sbt-native-packager takes the approach that SBT has built your source and generated
'binary' packages.

.. contents:: 
  :depth: 2
  
  
.. raw:: html

  <div class="alert alert-info" role="alert">
    <span class="glyphicon glyphicon-info-sign" aria-hidden="true"></span>
    The rpm plugin depends on the linux plugin. For general linux settings read the 
    <a href="linux.html">Linux Plugin Documentation</a>
  </div>
  
  
Requirements
------------

You need the following applications installed

* rpm

Build
-----

.. code-block:: bash

  sbt rpm:packageBin

Required Settings
~~~~~~~~~~~~~~~~~

A rpm package needs some mandatory settings to be valid. Make sure
you have these settings in your build:

.. code-block:: scala

    rpmVendor := "typesafe"


1.0 or higher
~~~~~~~~~~~~~

Enables the rpm plugin

.. code-block:: scala

  enablePlugins(RpmPlugin)


0.8 or lower
~~~~~~~~~~~~

For this versions rpm packaging is automatically activated.
See the :doc:`Getting Started </gettingstarted>` page for information
on how to enable sbt native packager.

Configuration
-------------

Settings and Tasks inherited from parent plugins can be scoped with ``Rpm``.

.. code-block:: scala

  linuxPackageMappings in Rpm := linuxPackageMappings.value

Settings
--------


Informational Settings
~~~~~~~~~~~~~~~~~~~~~~

  ``packageName in Rpm``
    The name of the package for the rpm.
    Its value defines the first component of the rpm file name
    (``packageName-version-rpmRelease.packageArchitecture.rpm``), as well as the ``Name:``
    tag in the spec file.
    Its default value is drawn from ``packageName in Linux``.

  ``version in Rpm``
    The version of the package for rpm.
    Takes the form ``x.y.z``, and note that there can be no dashes in this version string.
    It defines the second component of the rpm file name
    (``packageName-version-rpmRelease.packageArchitecture.rpm``), as well as the ``Version:``
    tag in the spec file.
    Its default value is drawn from the project defined ``version``.

  ``rpmRelease``
    The release number is the package's version. When the sofware is first packaged at a
    particular version, the release should be ``"1"``. If the software is repackaged at
    the same version, the release number should be incremented, and dropped back to ``"1"``
    when the software version is new.
    Its value defines the third component of the rpm file name
    (``packageName-version-rpmRelease.packageArchitecture.rpm``), as well as the ``Release:``
    tag in the spec file.
    Its default value is ``"1"``.

  ``packageArchitecture in Rpm``
    The build architecture for the binary rpm.
    Its value defines the fourth component of the rpm file name
    (``packageName-version-rpmRelease.packageArchitecture.rpm``), as well as the ``BuildArch:``
    tag in the spec file.
    Its default value is ``"noarch"``.

  ``packageSummary in Rpm``
    A brief, one-line summary of the package.
    Note, the summary **must not** contain line separators or end in a period.
    Its value defines the ``Summary:`` tag in the spec file, and its default
    value is drawn from ``packageSummary in Linux``.

  ``packageDescription in Rpm``
    A longer, multi-line description of the package.
    Its value defines the ``%description`` block in the spec file, and its
    default value is drawn from ``packageDescription in Linux``.

  ``rpmVendor``
    The name of the company/user generating the RPM.

  ``rpmUrl``
    A url associated with the software in the RPM.

  ``rpmLicense``
    The license associated with software in the RPM.

Dependency Settings
~~~~~~~~~~~~~~~~~~~

  ``rpmAutoreq``
    Enable or disable the automatic processing of required packages.
    Takes the form ``"yes"`` or ``"no"``, defaults to ``"yes"``.
    Defines the ``AutoReq:`` tag in the spec file.

  ``rpmRequirements``
    The RPM packages that are required to be installed for this RPM to work.

  ``rpmAutoprov``
    Enable or disable the automatic processing of provided packages.
    Takes the form ``"yes"`` or ``"no"``, defaults to ``"yes"``.
    Defines the ``AutoProv:`` tag in the spec file.

  ``rpmProvides``
    The RPM package names that this RPM provides.
    
  ``rpmPrerequisites``
    The RPM packages this RPM needs before installation
    
  ``rpmObsoletes``
    The packages this RPM allows you to remove
    
  ``rpmConflcits``
    The packages this RPM conflicts with and cannot be installed with.

Meta Settings
~~~~~~~~~~~~~

  ``rpmPrefix``
    The path passed set as the base for the revocable package

  ``rpmChangelogFile``
    External file to be imported and used to generate the changelog of the RPM.


Scriptlet Settings
~~~~~~~~~~~~~~~~~~
    
  ``rpmPretrans`` 
    The ``%pretrans`` scriptlet to run.
    
  ``rpmPre``
    The ``%pre`` scriptlet to run.
    
  ``rpmVerifyScript``
    The ``%verifyscript%`` scriptlet to run
    
  ``rpmPost``
    The ``%post`` scriptlet to run
    
  ``rpmPosttrans``
    The ``%posttrans`` scriptlet to run
    
  ``rpmPreun``
    The ``%preun`` scriptlet to run.
    
  ``rpmPostun``
    The ``%postun`` scriptlet to run.
    
  ``rpmBrpJavaRepackJars``
    appends ``__os_install_post`` scriptlet to ``rpmPre`` avoiding jar repackaging


Tasks
-----

The Rpm support grants the following commands:

  ``rpm:package-bin``
    Generates the ``.rpm`` package for this project.

  ``rpm:rpmlint``
    Generates the ``.rpm`` file and runs the ``rpmlint`` command to look for issues in the package.  Useful for debugging.


Customize
---------

Rpm Prefix
~~~~~~~~~~

The rpm prefix allows you to create a relocatable package as defined by http://www.rpm.org/max-rpm/s1-rpm-reloc-prefix-tag.html.
This optional setting with a handful of overrides to scriptlets and templates will allow you to create a working java_server
archetype that can be relocated in the file system.  


Example Settings:

.. code-block:: scala

    defaultLinuxInstallLocation := "/opt/package_root",
    rpmPrefix := Some(defaultLinuxInstallLocation),
    linuxPackageSymlinks := Seq.empty,
    defaultLinuxLogsLocation := defaultLinuxInstallLocation + "/" + name
  

rpmChangelogFile
~~~~~~~~~~~~~~~~

The rpmChangelogFile property allows you to set a source that will be imported and used on the RPM generation.
So if you use rpm commands to see the changelog it brings that information. You have to create the content on
that file following the RPM conventions that are available here http://fedoraproject.org/wiki/Packaging:Guidelines#Changelogs.

Example Settings:

.. code-block:: scala

    changelog := "changelog.txt"
    
    rpmChangelogFile := Some(changelog)


.. code-block:: bash

    * Sun Aug 24 2014 Team <contact@example.com> - 1.1.0
    -Allow to login using social networks
    * Wed Aug 20 2014 Team <contact@example.com> - 1.0.1
    -Vulnerability fix.
    * Tue Aug 19 2014 Team <contact@example.com> - 1.0.0
    -First version of the system


Template Changes
~~~~~~~~~~~~~~~~~~

Apply the following changes to the default init start script.  You can find this in the sbt-native-packager source.


``src/templates/start``

.. code-block:: bash
    
    ...
    [ -e /etc/sysconfig/$prog ] && . /etc/sysconfig/$prog
 
    # smb could define some additional options in $RUN_OPTS
    RUN_CMD="${PACKAGE_PREFIX}/${{app_name}}/bin/${{app_name}}"
    ...



Scriptlet Changes
~~~~~~~~~~~~~~~~~~

Changing the scripts can be done in two ways. Override the ``rpmPre``, etc. scripts
or place your new scripts in the ``src/rpm/scriptlets`` folder. For example:


``src/rpm/scriptlets/post-rpm``

.. code-block:: bash

    ...
    echo "PACKAGE_PREFIX=${RPM_INSTALL_PREFIX}" > /etc/sysconfig/${{app_name}}
    ...

``src/rpm/scriptlets/preun-rpm``

.. code-block:: bash

    ...
    rm /etc/sysconfig/${{app_name}}
    ...


    
Jar Repackaging
~~~~~~~~~~~~~~~

rpm repackages jars by default (described in this `blog post`_) in order to optimize jars.
This behaviour is turned off by default with this setting.

.. code-block:: scala

    rpmBrpJavaRepackJars := false
    
Note that this appends content to your ``rpmPre`` definition, so make sure not to override it.
For more information on this topic follow these links:

* `issue #195`_
* `pullrequest #199`_
* `OpenSuse issue`_

  .. _blog post: http://swaeku.github.io/blog/2013/08/05/how-to-disable-brp-java-repack-jars-during-rpm-build
  .. _issue #195: https://github.com/sbt/sbt-native-packager/issues/195
  .. _pullrequest #199: https://github.com/sbt/sbt-native-packager/pull/199
  .. _OpenSuse issue: https://github.com/sbt/sbt-native-packager/issues/215
  
