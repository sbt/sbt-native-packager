The application structure is customizable via the standard mappings, which is described in the
:ref:`Universal Plugin Section <universal-plugin>`.

Application and runtime configuration
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

There are generally two types of configurations:

* Configuring the JVM and the process
* Configuring the application itself

You have two options to define your runtime and application configurations.

Configuration file
^^^^^^^^^^^^^^^^^^

The start scripts provided by the ``BatStartScriptPlugin`` and ``BashStartScriptPlugin`` can both load an external
configuration file during execution. You can define the configuration file location for both with these two settings.


  ``bashScriptConfigLocation``
    The location of the bash script on the target system.

    **Default** ``${app_home}/../conf/application.ini``

  ``batScriptConfigLocation``
    The location of the bat script on the target system.

    **Default** ``%APP_HOME%\conf\application.ini``


The configuration path is the path on the **target** system. This means that native-packager needs to process this path
to create a valid ``universal:mapping``s entry.

* ``${app_home}/../`` is removed
* ``%APP_HOME%`` is removed and ``\`` is being replaced with ``/``

This means you can either

1. Create a configuration path relative to the application directory (recommended)
2. Create an absolute path that has to match your target **and** build system

**Example**

.. code-block:: scala

    // configure two different files for bash and bat
    bashScriptConfigLocation := Some("${app_home}/../conf/jvmopts-bash")
    batScriptConfigLocation  := Some("%APP_HOME%\\conf\\jvmopts-bat")


Now we know how to configure the location of our configuration file. The next step is to learn how to provide content
for the configuration file.

Via build.sbt
"""""""""""""

You can specify your options via the ``build.sbt``.

.. code-block:: scala

    javaOptions in Universal ++= Seq(
        // -J params will be added as jvm parameters
        "-J-Xmx64m",
        "-J-Xms64m",

        // others will be added as app parameters
        "-Dproperty=true",
        "-port=8080",

        // you can access any build setting/task here
       s"-version=${version.value}"
    )

For the ``-X`` settings you need to add a suffix ``-J`` so the start script will
recognize these as vm config parameters.

When you use the  ``javaOptions in Universal`` sbt-native-packager will generate configuration files
if you haven't set the ``batScriptConfigLocation`` and/or ``bashScriptConfigLocation`` to ``None``.

Via Application.ini
"""""""""""""""""""

The second option is to create ``src/universal/conf/application.ini`` with the following template

.. code-block:: bash

    # Setting -X directly (-J is stripped)
    # -J-X
    -J-Xmx1024

    # Add additional jvm parameters
    -Dkey=val

    # Turn on JVM debugging, open at the given port
    # -jvm-debug <port>

    # Don't run the java version check
    # -no-version-check

    # enabling debug and sending -d as app argument
    # the '--' prevents app-parameter swallowing when
    # using a reserved parameter. See #184
    # -d -- -d

The file will be installed to ``${app_home}/conf/application.ini`` and read from there
by the startscript. You can use ``#`` for comments and new lines as you like. This file
currently doesn't has any variable substitution. We recommend using the ``build.sbt`` if
you need any information from your build.

The configuration file for bash scripts takes arguments for the BASH file on each line,
and allows comments which start with the ``#`` character.  Essentially, this provides
a set of default arguments when calling the script.

By default, any file in the ``src/universal`` directory is packaged. This is a convenient
way to include things like licenses, and readmes.

If you don't like ``application.ini`` as a name, you can change this in the ``build.sbt``.
The default configuration looks like this

.. code-block:: scala

    bashScriptConfigLocation := Some("${app_home}/../conf/application.ini")
    batScriptConfigLocation := Some("%APP_HOME%\\conf\\application.ini")

.. _add-code-to-the-start-scripts:

Add code to the start scripts
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The second option is to add code to the generated start scripts via these settings.

  ``bashScriptExtraDefines``
    A list of extra definitions that should be written to the bash file template.

  ``batScriptExtraDefines``
    A list of extra definitions that should be written to the bat file template.


.. _bash-script-defines:

BashScript defines
""""""""""""""""""

The bash script accepts extra commands via ``bashScriptExtraDefines``. Generally you can add arbitrary
bash commands here, but for configurations you have two methods to add jvm and app parameters.

.. code-block:: scala

   // add jvm parameter for typesafe config
   bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/app.config""""
   // add application parameter
   bashScriptExtraDefines += """addApp "--port=8080""""

**Syntax**

  ``${{template_declares}}``
    Will be replaced with a series of ``declare <var>`` lines based on the ``bashScriptDefines`` key. These variables
    are predefined:
    * ``app_mainclass`` - The main class entry point for the application.
    * ``app_classpath`` - The complete classpath for the application (in order).


.. _bat-script-defines:

BatScript defines
"""""""""""""""""

The Windows batch script accepts extra commands via ``batScriptExtraDefines``. It offers
two methods to add jvm and app parameters using similar syntax to the BASH script.

.. code-block:: scala

   // add jvm parameter for typesafe config
   batScriptExtraDefines += """call :add_java "-Dconfig.file=%APP_HOME%\conf\app.config""""
   // add application parameter
   batScriptExtraDefines += """call :add_app "--port=8080""""

**Syntax**

  ``@@APP_ENV_NAME@@``
  will be replaced with the script friendly name of your package.

  ``@@APP_NAME@@``
  will be replaced with user friendly name of your package.

  ``@APP_DEFINES@@``
  will be replaced with a set of variable definitions, like ``APP_MAIN_CLASS``, ``APP_MAIN_CLASS``.


Start script customizations
~~~~~~~~~~~~~~~~~~~~~~~~~~~

While the native packager tries to provide robust BASH/BAT scripts for your applications, they may not always be enough.
The native packager provides a mechanism where the template used to create each script can be customized or directly
overridden.

Bash and Bat script extra defines
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

For the bat and bash script are separated settings available to add arbitrary code to the start script.
See :ref:`bash-script-defines` and :ref:`bat-script-defines` for details.

The  ``bashScriptExtraDefines`` sequence allows you to add new lines to the default bash script used to start the
application. This is useful when you need a setting which isn't mean for the command-line parameter list passed to the
java process. The lines added to ``bashScriptExtraDefines`` are placed near the end of the script and have access to a
number of utility bash functions (e.g. ``addJava``, ``addApp``, ``addResidual``, ``addDebugger``). You can add lines to
this script as we did for the Typesafe config file above. For more complex scripts you can also inject a separate file
managed in your source tree or resource directory:

.. code-block:: scala

    bashScriptExtraDefines ++= IO.readLines(baseDirectory.value / "scripts" / "extra.sh")

This will add the contents of ``/scripts/extra.sh`` in the resource directory to the bash script. Note you should always
concatenate lines to ``bashScriptExtraDefines`` as other stages in the pipeline may be include lines to the
start-script.



Overriding Templates (Bash/Bat)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. warning:: Replacing the default templates should really only be done if:

  1. There is a bug in one of the script templates you need to workaround
  2. There is a deficiency in the features of one of the templates you need to fix.

  In general, the templates are intended to provide enough utility that customization is only necessary for truly custom
  scripts.

In order to override full templates, like the default bash script, you can create a file in
``src/templates/bash-template``. Alternatively, you can use a different file location by setting
``bashScriptTemplateLocation``. There are


Similarly the windows BAT template can be overridden by placing a new template in ``src/templates/bat-template``.
You can also use a different file location by setting ``batScriptTemplateLocation``.
