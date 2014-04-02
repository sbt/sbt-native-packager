Getting Started with Servers
############################

The sbt-native-packager is an sbt plugin for bundling your server for a variety of platforms.  

**Note:** Please follow the :ref:`Installation` instructions for how to set it up on a project.

The sbt-native-packager attempts to make building packages for different operating systems easier.  While it provides
some basic abstractions around packaging, it also allows you to dig down into the nuts and bolts of each platform as
neeeded to generate the best package possible.   

Currently the native package supports the following installation targets for servers:

+---------------+--------------------+-----------+
| Platform      |  Service Manager   |  Working  |
+===============+====================+===========+
| Ubuntu        | Upstart            |    X      |
+---------------+--------------------+-----------+
| Ubuntu        | System V           |    X      |
+---------------+--------------------+-----------+
| CentOS        | System V           |    X      |
+---------------+--------------------+-----------+
| Fedora        | System V           |    X      |
+---------------+--------------------+-----------+
| Windows       | Windows Services   |           |
+---------------+--------------------+-----------+

