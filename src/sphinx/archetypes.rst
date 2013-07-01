.. _Archetypes:

Project Archetypes
==================

Project archetypes are default deployment scripts that try to "do the right thing" for a given type of project.
Because not all projects are created equal, there is no one single archetype for all native packages, but a set
of them for usage.

The architecture of the plugin is set up so that you can customize your packages at any level of complexity.  
For example, if you'd like to write Windows Installer XML by hand and manually map files, you should be able to do this while
still leveraging the default configuration for other platforms.


Curently, in the nativepackager these archetypes are available:

  * Java Command Line Application (Experimental)
  


  

Java Command Line Application
-----------------------------

A Java Command Line application is a Java application that consists of a set of JARs and a main method.  There is no
custom start scripts, or services.  It is just a bash/bat script that starts up a Java project.   To use
this archetype in your build, do the following in your ``build.sbt``:


    archetypes.java_application

    mapGenericFilesToLinux

    mapGenericFilesToWindows
    
    name := "A-package-friendly-name"
    
    packageSummary in Linux := "The name you want displayed in package summaries"

    packageSummary in Windows := "The name you want displayed in Add/Remove Programs"

    packageDescription := " A descriptioin of your project"

    maintainer in Windows := "Company"
    
    maintainer in Debian := "Your Name <your@email.com>"

    wixProductId := "ce07be71-510d-414a-92d4-dff47631848a"

    wixProductUpgradeId := "4552fb0e-e257-4dbd-9ecb-dba9dbacf424"
