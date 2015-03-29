.. _Archetypes:

.. toctree::
   :maxdepth: 2
   
   java_app/index.rst
   java_server/index.rst
   akka_app/index.rst
   cheatsheet.rst

Project Archetypes
==================

Project archetypes are default deployment scripts that try to "do the right thing" for a given type of project.
Because not all projects are created equal, there is no one single archetype for all native packages, but a set
of them for usage.

The architecture of the plugin is set up so that you can customize your packages at any level of complexity.
For example, if you'd like to write Windows Installer XML by hand and manually map files, you should be able to do this while
still leveraging the default configuration for other platforms.


Currently, in the nativepackager these archetypes are available:

  * Java Command Line Application
  * Java Server Application (Experimental - Debian Only)


Java Command Line Application
-----------------------------

A Java Command Line application is a Java application that consists of a set of JARs and a main method.  There is no
custom start scripts, or services.  It is just a bash/bat script that starts up a Java project.   To use
this archetype in your build, do the following in your ``build.sbt``:

.. code-block:: scala

    packageArchetype.java_application

    name := "A-package-friendly-name"

    packageSummary in Linux := "The name you want displayed in package summaries"

    packageSummary in Windows := "The name you want displayed in Add/Remove Programs"

    packageDescription := " A description of your project"

    maintainer in Windows := "Company"

    maintainer in Debian := "Your Name <your@email.com>"

    wixProductId := "ce07be71-510d-414a-92d4-dff47631848a"

    wixProductUpgradeId := "4552fb0e-e257-4dbd-9ecb-dba9dbacf424"


This archetype will use the ``mainClass`` setting of sbt (automatically discovers your main class) to generate ``bat`` and ``bin`` scripts for your project.  It
produces a universal layout that looks like the following:

.. code-block:: none

    bin/
      <app_name>       <- BASH script
      <app_name>.bat   <- cmd.exe script
    lib/
       <Your project and dependent jar files here.>


You can add additional files to the project by placing things in ``src/windows``, ``src/universal`` or ``src/linux`` as needed.

The scripts under ``bin`` will execute the ``main`` method of a class found in your application. But you can specific a custom main class method with the ``-main`` flag.

The default bash script also supports having a configuration file.  This config file can be used to specify default arguments to the BASH script.
To define a config location for your bash script, you can manually override the template defines:

.. code-block:: scala

    bashScriptConfigLocation := "$app_home/conf/my.conf"


This string can include any variable defines in the BASH script. In this case, ``app_home`` refers to the install location of the script.

Java Server
-----------

This archetype is designed for Java applications that are intended to run as
servers or services.  This archetype includes wiring an application to start
immediately upon startup. To activate this archetype replace ``packageArchetype.java_application`` with ``packageArchetype.java_server``.


The Java Server archetype has a similar installation layout as the java
application archetype. The primary differences are:

* Linux

  * ``/var/log/<pkg>`` is symlinked from ``<install>/logs``
  * Creates a start script in ``/etc/init.d`` or ``/etc/init/``
  * Creates a startup config file in ``/etc/default/<pkg>``



Akka Microkernel Application
----------------------------

An Akka microkernel application is similar to a Java Command Line application. Instead of running the classic ``mainClass``, 
an Akka microkernel application instantiates and runs a subclass of 
`Bootable <https://github.com/akka/akka/blob/master/akka-kernel/src/main/scala/akka/kernel/Main.scala>`_ . A minimal example
could look like this

.. code-block:: scala

    class HelloKernel extends Bootable {
      val system = ActorSystem("hellokernel")
     
      def startup = {
        // HelloActor and Start case object must of course be defined
        system.actorOf(Props[HelloActor]) ! Start
      }
     
      def shutdown = {
        system.terminate()
      }
    }

The *bash/bat* script that starts up the Akka application is copied from the Akka distribution. 

To use this archetype in your build, add the following to your ``build.sbt``:

.. code-block:: scala

    packageArchetype.akka_application

    name := "A-package-friendly-name"

    mainClass in Compile := Some("HelloKernel")

For more information take a look at the akka docs

* `Akka microkernel <http://doc.akka.io/docs/akka/snapshot/scala/microkernel.html>`_
* `akka.kernel.Main source <https://github.com/akka/akka/blob/master/akka-kernel/src/main/scala/akka/kernel/Main.scala>`_
* `akka.kernel.Bootable docs <http://doc.akka.io/api/akka/snapshot/index.html#akka.kernel.Bootable>`_

