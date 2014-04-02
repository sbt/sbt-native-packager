Adding configuration
####################

After :doc:`creating a package <MyFirstProject>`, the very next thing needed, usually, is the ability for users/ops to customize the application once it's deployed.   Let's add some configuration to the newly deployed application.

There are generally two types of configurations:

* Configuring the JVM and the process
* Configuring the Application itself.

The server archetype provides you with a special feature to configure your application
with a single file. As this file is OS dependend, each OS gets section.

Linux
*****

Create ``src/templates/etc-default`` with the following template

.. code-block :: bash

    # Available replacements 
    # ------------------------------------------------
    # ${{author}}           debian author
    # ${{descr}}            debian package description
    # ${{exec}}             startup script name
    # ${{chdir}}            app directory
    # ${{retries}}          retries for startup
    # ${{retryTimeout}}     retry timeout
    # ${{app_name}}         normalized app name
    # ${{daemon_user}}      daemon user
    # -------------------------------------------------

    # Setting -Xmx and -Xms in Megabyte
    # -mem 1024

    # Setting -X directly (-J is stripped)
    # -J-X
    # -J-Xmx1024

    # Add additional jvm parameters
    # -Dkey=val

    # For play applications you may set
    # -Dpidfile.path=/var/run/${{app_name}}/play.pid

    # Turn on JVM debugging, open at the given port
    # -jvm-debug <port>  

    # Don't run the java version check
    # -no-version-check
    
    # enabling debug and sending -d as app argument
    # the '--' prevents app-parameter swalloing when
    # using a reserved parameter. See #184
    # -d -- -d

The file will be installed to ``/etc/default/<normalizedName>`` and read from there
by the startscript.

Environment variables
=====================

The usual ``JAVA_OPTS`` can be used to override settings. This is a nice way to test
different jvm settings with just restarting the jvm.

Windows
*****

Support planned for 0.8.0

Example Configurations
######################

A list of very small configuration settings can be found at `sbt-native-packager-examples`_

    .. _sbt-native-packager-examples: https://github.com/muuki88/sbt-native-packager-examples
    
Next, let's :doc:`how to override start templates <OverrdingTemplates>`.
