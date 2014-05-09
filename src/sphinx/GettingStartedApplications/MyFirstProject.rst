My First Packaged Project
#########################

After installing the native packager, let's set up a raw sbt project to experiment with bundling things.  First, let's create a 
``project/build.properties`` file to save the sbt version ::

   sbt.version=0.13.1

sbt builds should always specify which version of sbt they are designed to use.  This helps keeps builds consistent between developers,
and documents to users which version of sbt you require for the build.

Next, let's add the native packager to our build by created a ``project/plugins.sbt`` file with the following contents ::

    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.7.0-RC1")

Now, the build needs to be configured for packaging.  Let's define the ``build.sbt`` file as follows

.. code-block:: scala

    name := "example-cli"

    version := "1.0"

    packageArchetype.java_application

The third line of ``build.sbt`` adds the default packaging settings for java applications. The native packager includes two 
"batteries included" options for packaging applications:

  * ``java_application`` - Defines packaging of your project with a start script and automatic PATH additions
  * ``java_server``      - Defines packaging of your project with automatic service start scripts (supports System V + init.d).

In addition to these, you can always directly configure all packaging by hand.   For now, we're using one of the built-in options
as these are pretty robust and configurable.

Now that the build is set up, Let's create an application that we can run on the command line.   Create the following file
``src/main/scala/TestApp.scala`` 

.. code-block:: scala

    object TestApp extends App {
      println("IT LIVES!")
    }

Once this is created, start ``sbt`` on the console and run the ``stage`` command ::

   $ sbt
   > stage

Now, in another terminal, let's look at what was generated ::

    target/universal/stage/
      bin/
        example-cli
        example-cli.bat
      lib/
        example-cli.example-cli-1.0.jar
        org.scala-lang.scala-library-2.10.3.jar

By default, the plugin has created both a windows BAT file and a linux/mac bash script for running the application.
In addition, all the dependent jars are added into the ``lib/`` folder.   Let's try out the script in a terminal ::

    $ ./target/universal/stage/bin/example-cli 
    IT LIVES!
    $

Now that the package has been verified, let's work on the generic or "universal" packaging.   This is when
the plugin packages your application in a simple format that should be consumable from most operating systems or
platforms.  There are two ways to do this in the sbt console ::

    > universal:packageBin
    [info] /home/jsuereth/projects/sbt/sbt-native-packager/tutorial-example/target/universal/example-cli-1.0.zip

    > universal:packageZipTarball
    [info] /home/jsuereth/projects/sbt/sbt-native-packager/tutorial-example/target/universal/example-cli-1.0.tgz

This task simple constructs either a tgz or zip file with the exact same contents we found in the staged directory.

While this is a great first step towards deploying our application, we'd like to make it even simpler.  Our target
deployment platform is Ubuntu.  The command line tool should be usable by all our developers with a very simple
installation and update mechanism.   So, let's try to make a debian out of our package.  Try the ``debian:packageBin`` task in the sbt console ::

    > debian:packageBin
    [trace] Stack trace suppressed: run last debian:debianControlFile for the full output.
    [error] (debian:debianControlFile) packageDescription in Debian cannot be empty. Use 
    [error]                  packageDescription in Debian := "My package Description"
    [error] Total time: 0 s, completed Apr 1, 2014 10:21:13 AM

Here, the native packager is warning that we haven't fully configured all the information required to genreate a valid debian file.  In particular, the packageDescription needs to be filled out for debian, in addition to a few other settings.   Let's add the debian configuration to ``build.sbt`` ::

    name := "example-cli"

    version := "1.0"

    packageArchetype.java_application

    packageDescription in Debian := "Example Cli"

    maintainer in Debian := "Josh Suereth"

Now, let's try to run the ``debian:packageBin`` command in the sbt console again ::

    $ sbt
    > debian:PacakgeBin
    [info] Altering postrm/postinst files to add user example-cli and group example-cli
    [info] dpkg-deb: building package `example-cli' in `/home/jsuereth/projects/sbt/sbt-native-packager/tutorial-example/target/example-cli-1.0.deb'

This generates a debian file that will install the following owners and files ::

    root:root                /usr/
    examplecli:examplecli      share/example-cli/
    examplecli:examplecli        bin/
    examplecli:examplecli          example-cli
    examplecli:examplecli        lib/
    examplecli:examplecli          example-cli.example-cli-1.0.jar
    examplecli:examplecli          org.scala-lang.scala-library-2.10.3.jar
    root:root                  bin/
    root:root                    example-cli -> ../share/example-cli/bin/example-cli

So, the default packaing takes the "universal" distribution and places it inside a ``/usr/share`` directory, owned by a user for the application.   In addition, there is a a symlink in ``/usr/bin`` to the distributed bin script.  This allows users on the platform to run the ``example-cli`` as a native install.

We can generate other packages via the following tasks.  Here's a complete list of current options.

* ``universal:packageBin`` - Generates a universal zip file
* ``universal:packageZipTarball`` - Generates a universal tgz file
* ``debian:packageBin`` - Generates a deb
* ``rpm:packageBin`` - Generates an rpm
* ``universal::packageOsxDmg`` - Generates a DMG file with the same contents as the universal zip/tgz.
* ``windows:packageBin`` - Generates an MSI 

While we only covered the necessary configuration for ``debian``, each package type beyond ``universal`` requires some additonal
configuration relative to that packager.  For example, windows MSIs require UUIDs for all packages which are used to uniquely
identifiy two packages that may have the same name.

Next, let's look at how to :doc:`Add configuration files <AddingConfiguration>` to use with our script.


