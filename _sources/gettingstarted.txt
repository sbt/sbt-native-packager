Getting Started
===============

The sbt-native-packager is an sbt plugin.  Please follow the [[installation]] instructions for how to set it up on a project.

The sbt-native-packager attempts to make building packages for different operating systems easier.  While it provides
some basic abstractions around packaging, it also allows you to dig down into the nuts and bolts of each platform as
neeeded to generate the best package possible.   

Most packages are split into two types of deployments:

1. 'nix-style deployments.  Pushing scripts to ``/usr/bin``, docs to ``/usr/share/man``, etc.
2. Windows/Universal-style deployments.  A single directory holds all the artifacts needed to run the application.

Because of this, the Native packager splits defining of packages into two pieces:  ``mappings in Universal`` and ``linuxPackageMappings``.



Defining a new package
~~~~~~~~~~~~~~~~~~~~~~

TODO - Write more.



Note: That as of the latest version of the native packager plugin, project Archetypes are coming into existing, which
drastically simplify creating native packages if your application fits a certain mold.