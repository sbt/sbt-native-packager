Adding configuration
####################

After :doc:`creating a package <MyFirstProject>`, the very next thing needed, usually, is the ability for users/ops to customize the application once it's deployed.   Let's add some configuration to the newly deployed application.

There are generally two types of configurations:

* Configuring the JVM and the process
* Configuring the Application itself.

The native packager provides a direct hook into the generated scripts for JVM configuration. Let's make use of this.  First, add the following to the ``src/universal/conf/jvmopts`` file in the project ::

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

Now that we have ability to configure the JVM, let's add in a more robust method of customizing the applciation.  We'll be using the `Typesafe Config <https://github.com/typesafehub/config>`_ library for this purpose.

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

This line modifies the generated BASH script to add the JVM options the location of the application configuration on disk.  Now, let's modify the application (``src/main/scala/TestApp.scala``) to read this configuration ::

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

Here, we can see that the entire ``conf`` directory for the application is exposed on ``/etc`` as is standard for other linux applications.  By convention, all files in the universal ``conf`` directory are marked as configuration files when packaged, allowing users to modify them.

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
--------------------

As you read earlier the ``bashScriptExtraDefines`` sequence allows you to add new lines to the default bash script used to start the application.
This is useful when you need a setting which isn't mean for the command-line parameter list passed to the java process. The lines added to
``bashScriptExtraDefines`` are placed near the end of the script and have access to a number of utility bash functions (e.g. ``addJava``,
``addApp``, ``addResidual``, ``addDebugger``). You can add lines to this script as we did for the Typesage config file above. For more complex
scripts you can also inject a seperate file managed in your source tree or resource directory: ::

    bashScriptExtraDefines ++= IO.readLines(baseDirectory.value / "scripts" / "extra.sh")

This will add the contents of ``/scripts/extra.sh`` in the resource directory to the bash script. Note you should always concatenate lines
to ``bashScriptExtraDefines`` as other stages in the pipeline may be include linex to the start-script. 

Next, let's :doc:`add some generated files <GeneratingFiles>`.
