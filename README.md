# SBT Native Packager #

[![Build Status](https://api.travis-ci.org/sbt/sbt-native-packager.png?branch=master)](https://travis-ci.org/sbt/sbt-native-packager)

This is a work in progress project.  The goal is to be able to bundle up Scala software built with SBT for native packaging systems, like deb, rpm, homebrew, msi.


## Issues/Discussions

*  **Discussion/Questions**:
  If you wish to ask questions about the native packager, we have a [mailinglist](https://groups.google.com/forum/#!forum/sbt-native-packager) and
  we're very active on [Stack Overflow](http://stackoverflow.com/questions/tagged/sbt). You can either use the `sbt` tag or the
  `sbt-native-packager` tag.  They also have far better search support for working around issues.
* **Docs**:
  Our docs are [available online](http://scala-sbt.org/sbt-native-packager).  If you'd like to help improve the docs, they're part of this
  repository in the `src/sphinx` directory.
* **Issues/Feature Requests**:
  Finally, any bugs or features you find you need, please report to our [issue tracker](https://github.com/sbt/sbt-native-packager/issues/new).
  Please check the [compatibility matrix](https://github.com/sbt/sbt-native-packager/wiki/Tested-On) to see if your system is able to
  produce the packages you want.

## Installation ##

Add the following to your `project/plugins.sbt` file:

```scala
// for sbt 0.12.x and scala 2.9.x
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.7.5")

// for sbt 0.13.x and scala 2.10.x
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.8.0-M2")
```

For the native packager keys add this to your `build.sbt`

```scala
import NativePackagerKeys._
```
    


## Experimental systemd bootsystem support ##

Native packager now provides experimental `systemd` startup scripts.
Currently it works on Fedora `Fedora release 20 (Heisenbug)` and doesn't work on Ubuntu because of partial `systemd` support in `Ubuntu 14.04 LTS`. 
To enable this feature follow [My First Packaged Server Project guide](http://www.scala-sbt.org/sbt-native-packager/GettingStartedServers/MyFirstProject.html) and use `Systemd` as server loader:
  
    import com.typesafe.sbt.packager.archetypes.ServerLoader.Systemd
    serverLoading in Rpm := Systemd
  
Any help on testing and improving this feature is appreciated so feel free to report bugs or making PR.

## Experimental Docker support ##

Native packager now provides experimental `Docker` images.
To enable this feature follow [My First Packaged Server Project guide](http://www.scala-sbt.org/sbt-native-packager/GettingStartedServers/MyFirstProject.html) and use one of the provided Docker tasks for generating images.
The only essential extra setting for creating a local image for testing is:

    maintainer in Docker := "John Smith <john.smith@example.com>"

To publish the image, ``dockerRepository`` should also be set.

As with the `systemd` support, help with testing and improvements is appreciated.

## Experimental JDeb support ##

If don't run a linux or mac system with ``dpkg`` installed, you can configure the
debian packaging be done by [jdeb](https://github.com/tcurdt/jdeb). To enable this just set another packaging
implementation in your `build.sbt`

```scala
packageBin in Debian <<= debianJDebPackaging in Debian
```

## Documentation ##

There's a complete "getting started" guide and more detailed topics available at [the sbt-native-packager site](http://scala-sbt.org/sbt-native-packager).  

Please feel free to [contribute documentation](https://github.com/sbt/sbt-native-packager/tree/master/src/sphinx), or raise issues where you feel it may be lacking.


