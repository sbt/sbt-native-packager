Project Archetypes
==================

Project archetypes are default deployment scripts that try to "do the right thing" for a given type of project.
Because not all projects are created equal, there is no one single archetype for all native packages, but a set
of them for usage.

Curently, in the nativepackager these archetypes are available:

  * Java Vanilla Application
  
  

Java Vanilla Application
------------------------

A Java vanilla application is a Java application that consists of a set of JARs and a main method.  There is no
custom start scripts, or services.  It is just a bash/bat script that starts up a Java project.   To use
this archetype in your build, do the following in your ``build.sbt``:


    archetypes.java_application
    
    name := "A-package-friendly-name"
    
    packageSummary in Linux := "The name you want displayed in package summaries",

    packageDescription in Linux := " A descriptioin of your project",
    
    maintainer in Debian := "Your Name <your@email.com>"