Customize Java Applications
###########################

While the native packager tries to provide robust BASH/BAT scripts for your applications, they may not always be enough.
The native packager provides a mechanism where the template used to create each script can be customized or directly 
overridden. 

The easiest way to add functionality to the default script is by adding ``bashScriptExtraDefines`` :doc:` as described
in adding configuration for applications </archetypes/java_app/customize>`. Customizing the bash
script will effect all platform-specific builds. The server archetype provides a further level of customization for
specific System Loaders and Package types. These template file are described in 
:doc:`configuring servers </archetypes/java_server/customize>`.

Customizing Templates (Bash/Bat)
--------------------------------

.. raw:: html

  <div class="alert alert-info" role="alert">
    <span class="glyphicon glyphicon-info-sign" aria-hidden="true"></span>
    If you plan to use the Java Server Archetype you have <a href="../java_server/customize.html">other options to configure
    your application</a>.<br> This section is for non-server, standalone applications. However everything will work for server
    applications as well. 
  </div>

After :doc:`creating a package <my-first-project>`, the very next thing needed, usually, is the ability for users/ops to customize
the application once it's deployed.   Let's add some configuration to the newly deployed application.

There are generally two types of configurations:

* Configuring the JVM and the process
* Configuring the Application itself.

The native packager provides a direct hook into the generated scripts for JVM configuration. Let's make use of this.
First, add the following to the ``src/universal/conf/jvmopts`` file in the project ::

   -DsomeProperty=true

Now, if we run the ``stage`` task, we'll see this file show up in the distribution ::

   $ sbt stage
   $ ls target/universal/stage
      bin/
      conf/
      lib/
   $ ls target/universal/stage/conf
      jvmopts

By default, any file in the ``src/universal`` directory is packaged.  This is a convenient way to include things like licenses, and readmes.

Now, we need to modify the script templates to load this configuration.  To do so, add the following
to ``build.sbt`` ::

    bashScriptConfigLocation := Some("${app_home}/../conf/jvmopts")

Here, we define the configuration location for the BASH script too look for the ``conf/jvmopts`` file.  Now, let's run ``sbt stage`` and then execute the script in debug mode to see what command line it executes ::

    ./target/universal/stage/bin/example-cli -d
        # Executing command line:
        java
        -Xms1024m
        -Xmx1024m
        -XX:MaxPermSize=256m
        -XX:ReservedCodeCacheSize=128m
        -DsomeProperty=true
        -cp
        /home/jsuereth/projects/sbt/sbt-native-packager/tutorial-example/target/universal/stage/lib/example-cli.example-cli-1.0.jar:/home/jsuereth/projects/sbt/sbt-native-packager/tutorial-example/target/universal/stage/lib/org.scala-lang.scala-library-2.10.3.jar:/home/jsuereth/projects/sbt/sbt-native-packager/tutorial-example/target/universal/stage/lib/com.typesafe.config-1.2.0.jar
        TestApp


The configuration file for bash scripts takes arguments for the BASH file on each line, and allows comments which start with the ``#`` character.  Essentially, this provides a set of default arguments when calling the script.

Now that we have ability to configure the JVM, let's add in a more robust method of customizing the application.  We'll be using the `Typesafe Config <https://github.com/typesafehub/config>`_ library for this purpose.

First, let's add it as a dependency in ``build.sbt`` ::

   libraryDependencies += "com.typesafe" % "config" % "1.2.0"

Next, let's create the configuration file itself.  Add the following to ``src/universal/conf/app.config`` ::

    example {
      greeting = "Hello, World!"
    }

Now, we need a means of telling the typesafe config library where to find our configuration.  The library supports
a JVM property "``config.file``" which it will use to look for configuration.   Let's expose this file
in the startup BASH script.  To do so, add the following to ``build.sbt`` ::

    bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/app.config""""

This line modifies the generated BASH script to add the JVM options the location of the application configuration on disk.  Now, let's modify the application (``src/main/scala/TestApp.scala``) to read this configuration

.. code-block:: scala

    import com.typesafe.config.ConfigFactory
    
    object TestApp extends App {
      val config = ConfigFactory.load()
      println(config.getString("example.greeting"))
    }

Now, let's try it out on the command line ::

    $ sbt stage
    $ ./target/universal/stage/bin/example-cli
    Hello, World!


Finally, let's see what this configuration looks like in a linux distribution.  Let's run the debian packaging again ::

    $ sbt debian:packageBin

The resulting structure is the following ::

    /usr/
      share/example-cli/
        conf/
          app.config
          jvmopts
        bin/
          example-cli
        lib/
          example-cli.example-cli-1.0.jar
          org.scala-lang.scala-library-2.10.3.jar
      bin/
        example-cli -> ../share/example-cli/bin/example-cli
    /etc/
       example-cli -> /usr/share/example-cli/conf

Here, we can see that the entire ``conf`` directory for the application is exposed on ``/etc`` as is standard for
other linux applications.  By convention, all files in the universal ``conf`` directory are marked as configuration
files when packaged, allowing users to modify them.

Configuring for Windows
~~~~~~~~~~~~~~~~~~~~~~~
While we just covered how to do configuration for linux/mac, windows offers some subtle differences.

First, while the BASH file allows you to configure where to load JVM options and default arguments, in
windows we can only configure JVM options.  The path is hardcoded, as well to:

``<install directory>/@@APP_ENV_NAME@@_config.txt``

where ``@@APP_ENV_NAME@@`` is replaced with an environment friendly name for your app.   In this example, that would be: ``EXAMPLE_CLI``.

We can provide a configuration for JVM options on windows by creating a ``src/universal/EXAMPLE_CLI_config.txt`` file with the following contents ::

    -Xmx512M
    -Xms128M

This will add each line of the file as arguments to the JVM when running your application.


Now, if we want to add the typesafe config library again, we need to write the ``config.file`` property into the JVM options again.

One means of doing this is hooking the ``batScriptExtraDefines`` key.  This allows us to insert various BAT settings/commands into the script.  Let's use this to hook the config file location, using the other variables in the BASH script.  Modify your ``build.sbt`` as follows  ::

    batScriptExtraDefines += """set _JAVA_OPTS=%_JAVA_OPTS% -Dconfig.file=%EXAMPLE_CLI_HOME%\\conf\\app.config"""

Now, the windows version will also load the configuration from the ``conf/`` directory of the package.

More Complex Scripts
~~~~~~~~~~~~~~~~~~~~

As you read earlier the ``bashScriptExtraDefines`` sequence allows you to add new lines to the default bash script used to start the application.
This is useful when you need a setting which isn't mean for the command-line parameter list passed to the java process. The lines added to
``bashScriptExtraDefines`` are placed near the end of the script and have access to a number of utility bash functions (e.g. ``addJava``,
``addApp``, ``addResidual``, ``addDebugger``). You can add lines to this script as we did for the Typesafe config file above. For more complex
scripts you can also inject a separate file managed in your source tree or resource directory: ::

    bashScriptExtraDefines ++= IO.readLines(baseDirectory.value / "scripts" / "extra.sh")

This will add the contents of ``/scripts/extra.sh`` in the resource directory to the bash script. Note you should always concatenate lines
to ``bashScriptExtraDefines`` as other stages in the pipeline may be include lines to the start-script.



Overriding Templates (Bash/Bat)
-------------------------------

In order to override full templates, like the default bash script, create a file in ``src/templates/bash-template`` 

.. code-block:: bash

    #!/usr/bin/env bash

    realpath() {
      # TODO - The original bash template has a robust mechanism to find the true
      #        path to your application, following multiple symlinks.
      #        
    }

    addJava() {
      # Here we override the original templates addJava method to do nothing,
      # since this was how we were adding configuration before.
    }

    declare -r real_script_path="$(realpath "$0")"

    # We have to provide an app_home for the default bash declarations to work.
	declare -r app_home="$(realpath "$(dirname "$real_script_path")")"

	# The auto-generated classpath relies on this variable existing
	# and pointing at the lib directory.
    declare -r lib_dir="$(realpath "${app_home}/../lib")"

    # This line tells the native packager template engine to inject
    # all of its settings into this spot in the bash file.
    ${{template_declares}}

    # Here we make use of two of the injected settings for the bash file:
    # * app_classpath - represents the full list of JARs for this application.
    # * app_mainclass - represents the class with a main method we should call.
    exec java -cp $app_classpath $app_mainclass $@


Similarly the windows BAT template can be overridden by placing a new template in ``src/templates/bat-template``

.. code-block:: bat

    @REM A bat starter script
    @echo off

    @REM Here we need to set up a "home" variable for our classpath.
    @REM The APP_ENV_NAME variable is replaced by the packager template engine
    @REM with an "environment variable friendly" name for the app.
    if "%@@APP_ENV_NAME@@_HOME%"=="" set "@@APP_ENV_NAME@@_HOME=%~dp0\\.."
    set "APP_LIB_DIR=%@@APP_ENV_NAME@@_HOME%\lib\"

    @REM - This tells the template engine to inject any custom defines into our bat file here.
    @@APP_DEFINES@@

    @REM - Here we use the provided APP_CLASSPATH and APP_MAIN_CLASS parameters
    java -cp "%APP_CLASSPATH%" %APP_MAIN_CLASS% %*


While we just replaced the default templates with simpler templates, this should really only be done if:

1. There is a bug in one of the script templates you need to workaround
2. There is a deficiency in the features of one of the templates you need to fix.

In general, the templates are intended to provide enough utility that customization is only necessary for truly custom scripts.


``src/templates/bat-template``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Creating a file here will override the default template used to
generate the ``.bat`` script for windows distributions.

**Syntax**

``@@APP_ENV_NAME@@`` - will be replaced with the script friendly name of your package.

``@@APP_NAME@@`` - will be replaced with user friendly name of your package.

``@APP_DEFINES@@`` - will be replaced with a set of variable definitions, like
  ``APP_MAIN_CLASS``, ``APP_MAIN_CLASS``.

You can define additional variable definitions using ``batScriptExtraDefines``.

``src/templates/bash-template``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Creating a file here will override the default template used to
generate the BASH start script found in ``bin/<application>`` in the
universal distribution

**Syntax**

``${{template_declares}}`` - Will be replaced with a series of ``declare <var>``
lines based on the ``bashScriptDefines`` key.  You can add more defines to
the ``bashScriptExtraDefines`` that will be used in addition to the default set:

* ``app_mainclass`` - The main class entry point for the application.
* ``app_classpath`` - The complete classpath for the application (in order).

Next, let's look at how to :doc:`document the application <writing-documentation>`.
