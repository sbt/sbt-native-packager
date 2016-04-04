Play 2 Packaging
================

Although Play 2 supports Sbt Native Packager, it requires some additional steps to successfully package and run your
application.

.. tip:: there are also two sections in the play documentation that describe deploying and configuring:

    - `play deploying`_
    - `play prod configuration`_

.. _play deploying: https://playframework.com/documentation/2.5.x/Deploying
.. _play prod configuration: https://playframework.com/documentation/2.5.x/ProductionConfiguration

Build Configuration
-------------------

Depending on whether you want to package your application as a deb-package or
as an rpm-package, you have to setup your build configuration accordingly.
Please, refer to :ref:`debian-plugin` and :ref:`rpm-plugin`
pages for additional information.

Note that **Upstart** is not supported by all available operation systems and may not always work as expected.
You can always fallback to the **SystemV** service manager instead.
For more information on service managers please refer to :ref:`java-server-plugin` page.

Application Configuration
-------------------------

In order to run your application in production you need to provide it with at least:

* Location where it can store its pidfile
* Production configuration

One way to provide this information is to append the following content in your build definition:

.. code-block:: scala

  javaOptions in Universal ++= Seq(
    // JVM memory tuning
    "-J-Xmx1024m",
    "-J-Xms512m",

    // Since play uses separate pidfile we have to provide it with a proper path
    // name of the pid file must be play.pid
    s"-Dpidfile.path=/var/run/${packageName.value}/play.pid",

    // alternative, you can remove the PID file
    // s"-Dpidfile.path=/dev/null",

    // Use separate configuration file for production environment
    s"-Dconfig.file=/usr/share/${packageName.value}/conf/production.conf",

    // Use separate logger configuration file for production environment
    s"-Dlogger.file=/usr/share/${packageName.value}/conf/production-logger.xml",

    // You may also want to include this setting if you use play evolutions
    "-DapplyEvolutions.default=true"
  )

This way you should either store your production configuration under ``${{path_to_app_name}}/conf/production.conf``
or put it under ``/usr/share/${{app_name}}/conf/production.conf`` by hand or using some configuration management system.

.. warning:: Your pid file must be called **play.pid**



SystemV
~~~~~~~

If you use a system using SystemV start script make sure to provide
a `etc-default` in `src/templates` and set the `PIDFILE` environment variable.


.. code-block :: bash

    # Setting JAVA_OPTS
    # -----------------
    # you can use this instead of the application.ini as well
    # JAVA_OPTS="-Dpidfile.path=/var/run/${{app_name}}/play.pid $JAVA_OPTS"

    # For rpm/systemv you need to set the PIDFILE env variable as well
    PIDFILE="/var/run/${{app_name}}/play.pid"


See customize  section for :ref:`java-server-plugin` for more information on *application.ini* and *etc-default*
template.
