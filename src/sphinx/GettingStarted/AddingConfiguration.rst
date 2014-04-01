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
   $ ls target/unviersal/stage/conf
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

Now that we have a basic application created, let's write some documentation for it.