.. _rpm-plugin:

Rpm Plugin
==========

RedHat ``rpm`` files support a number of very advanced features.  To take full advantage of this environment,
it's best to understand how the ``rpm`` package system works. `How to create an RPM package`_ on the fedorda project wiki
is a good tutorial, but it focuses on building packages from *source.*  The sbt-native-packager assumes that SBT has built your source and generated
*binary* packages.

.. note:: The rpm plugin depends on the :ref:`linux-plugin`.

.. _How to create an RPM package: http://fedoraproject.org/wiki/How_to_create_an_RPM_package

Requirements
------------

You need the following applications installed

* rpm
* rpm-build

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
    Note: the summary **must not** contain line separators or end in a period.
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

  ``rpmSetarch[SettingKey[Option[String]]]``
    Run rpmbuild via Linux ``setarch`` command.  Use this for cross-platform builds.

Meta Settings
~~~~~~~~~~~~~

  ``rpmPrefix``
    The path passed set as the base for the revocable package

  ``rpmChangelogFile``
    External file to be imported and used to generate the changelog of the RPM.


Scriptlet Settings
~~~~~~~~~~~~~~~~~~

  ``maintainerScripts in Rpm``
    Contains the scriptlets being injected into the specs file. Currently supports all
    previous scriptlets: ``%pretrans``, ``%pre``, ``%verifyscript%``, ``%post``, ``%posttrans``,
    ``%preun`` and  ``%postun``

  ``rpmBrpJavaRepackJars``
    appends ``__os_install_post`` scriptlet to ``rpmPre`` avoiding jar repackaging


SystemV Start Script Settings
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  ``rpmDaemonLogFile``
    File name of the log generated by application daemon.


Tasks
-----

The Rpm support grants the following commands:

  ``rpm:package-bin``
    Generates the ``.rpm`` package for this project.

  ``rpm:rpm-lint``
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

Scriptlet Changes
~~~~~~~~~~~~~~~~~

Changing the scripts can be done in two ways. Override the ``maintainerScripts in Rpm``.
For example:

.. code-block:: scala

   // overriding
   import RpmConstants._
   maintainerScripts in Rpm := Map(
     Pre -> Seq("""echo "pre-install""""),
     Post -> Seq("""echo "post-install""""),
     Pretrans -> Seq("""echo "pretrans""""),
     Posttrans -> Seq("""echo "posttrans""""),
     Preun -> Seq("""echo "pre-uninstall""""),
     Postun -> Seq("""echo "post-uninstall"""")
   )

   // appending with strings and replacements
   import RpmConstants._
   maintainerScripts in Rpm := maintainerScriptsAppend((maintainerScripts in Rpm).value)(
      Pretrans -> "echo 'hello, world'",
      Post -> s"echo 'installing ${(packageName in Rpm).value}'"
   )

   // appending from a different file
   import RpmConstants._
   maintainerScripts in Rpm := maintainerScriptsAppendFromFile((maintainerScripts in Rpm).value)(
      Pretrans -> (sourceDirectory.value / "rpm" / "pretrans"),
      Post -> (sourceDirectory.value / "rpm" / "posttrans")
   )

The helper methods can be found in `MaintainerScriptHelper Scaladocs`_.

You also can place new scripts in the ``src/rpm/scriptlets`` folder. For example:


``src/rpm/scriptlets/preinst``

.. code-block:: bash

    ...
    echo "PACKAGE_PREFIX=${RPM_INSTALL_PREFIX}" > /etc/sysconfig/${{app_name}}
    ...

``src/rpm/scriptlets/preun``

.. code-block:: bash

    ...
    rm /etc/sysconfig/${{app_name}}
    ...

Using files will override all previous contents. The names used can be found in
the `RPM Scaladocs`_.

Scriptlet Migration from 1.0.x
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Before

.. code-block:: scala

    rpmPostun := rpmPost.value.map { content =>
      s"""|$content
         |echo "I append this to the current content
         |""".stripMargin
      }.orElse {
       Option("""echo "There wasn't any previous content"
       """.stripMargin)
      }

After

.. code-block:: scala

    // this gives you easy access to the correct keys
    import RpmConstants._
    // in order to append you have to pass the initial maintainerScripts map
    maintainerScripts in Rpm := maintainerScriptsAppend((maintainerScripts in Rpm).value)(
       Pretrans -> "echo 'hello, world'",
       Post -> s"echo 'installing ${(packageName in Rpm).value}'"
    )


Jar Repackaging
~~~~~~~~~~~~~~~

rpm repackages jars by default (described in this `blog post`_) in order to optimize jars.
This behaviour is turned off by default with this setting.

.. code-block:: scala

    rpmBrpJavaRepackJars := false

Note that this appends content to your ``Pre`` definition, so make sure not to override it.
For more information on this topic follow these links:

* `issue #195`_
* `pullrequest #199`_
* `OpenSuse issue`_

  .. _blog post: http://swaeku.github.io/blog/2013/08/05/how-to-disable-brp-java-repack-jars-during-rpm-build
  .. _issue #195: https://github.com/sbt/sbt-native-packager/issues/195
  .. _pullrequest #199: https://github.com/sbt/sbt-native-packager/pull/199
  .. _OpenSuse issue: https://github.com/sbt/sbt-native-packager/issues/215
  .. _RPM Scaladocs: http://www.scala-sbt.org/sbt-native-packager/latest/api/#com.typesafe.sbt.packager.rpm.RpmPlugin$$Names$
  .. _MaintainerScriptHelper Scaladocs: http://www.scala-sbt.org/sbt-native-packager/latest/api/#com.typesafe.sbt.packager.MaintainerScriptHelper$
