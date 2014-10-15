Getting Started with Applications
#################################

The sbt-native-packager is an sbt plugin for bundling your application for a variety of platforms.  

**Note:** Please follow the :ref:`Installation` instructions for how to set it up on a project.

The sbt-native-packager attempts to make building packages for different operating systems easier.  While it provides
some basic abstractions around packaging, it also allows you to dig down into the nuts and bolts of each platform as
neeeded to generate the best package possible. 

Application packaging focuses on how your application is launched (via a ``bash`` or ``bat`` script), how dependencies
are managed and how configuration and other auxillary files are included in the final distributable.

Additionally there is :doc:`Server Packaging <GettingStartedServers>` which provides platform-specific
functionality for installing your application in server environments. You can customize specific debian and rpm packaging
for a variety of platforms and init service loaders including Upstart, System V and SystemD (experimental).

Sbt-Native-Packager is highly customizable and you can add or override several aspects of application and server packaging.

.. toctree::
   :maxdepth: 1
   
   MyFirstProject.rst
   AddingConfiguration.rst
   GeneratingFiles.rst
   OverridingTemplates.rst
   WritingDocumentation.rst
