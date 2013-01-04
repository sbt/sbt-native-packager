RedHat
======

RedHat ``rpm`` files support a very advanced number of features.  To take full advantage of this environment, it's best to understand how the ``rpm`` package system works.  http://fedoraproject.org/wiki/How_to_create_an_RPM_package is a good tutorial, but it focuses on building packages from source.   The sbt-native-packager takes the approach that SBT has built your source and generated 'binary' packages.

Settings
--------

Rpms require the following specific settings:

name in Rpm := "sbt",
    version in Rpm <<= sbtVersion.identity,
    rpmRelease := "1",
    rpmVendor := "typesafe",
    rpmUrl := Some("http://github.com/paulp/sbt-extras"),
    rpmLicense := Some("BSD"),


Informational Settings
~~~~~~~~~~~~~~~~~~~~~~

  ``name in Rpm``
    The name of the package for rpm (if different from general linux name).

  ``version in Rpm``
    The version of the package for rpm (if different from general version).  Takes the form ``x.y.z``.

  ``rpmRelease``
    A release number the denotes the `rpm` version relative to the underlying software.

  ``rpmVendor``
    The name of the company/user generating the RPM.

  ``rpmUrl``
    A url associated with the software in the RPM.

  ``rpmLicense``
    The license associated with software in the RPM.

Dependency Settings
~~~~~~~~~~~~~~~~~~~

  ``rpmRequirements``
    The RPM packages that are required to be installed for this RPM to work.
    
    ``rpmProvides``
    The RPM package names that this RPM provides.
    
    ``rpmPrerequisites``
    The RPM packages this RPM needs before installation
    
    ``rpmObsoletes``
    The packages this RPM allows you to remove
    
    ``rpmConflcits``
    The packages this RPM conflicts with and cannot be installed with.


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


Tasks
-----

The Rpm support grants the following commands:

  ``rpm:package-bin``
    Generates the ``.rpm`` package for this project.

  ``rpm:rpmlint``
    Generates the ``.rpm`` file and runs the ``rpmlint`` command to look for issues in the package.  Useful for debugging.
