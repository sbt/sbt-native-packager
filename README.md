# SBT Native Packager #

[![Build Status](https://api.travis-ci.org/sbt/sbt-native-packager.png?branch=master)](https://travis-ci.org/sbt/sbt-native-packager)

This is a work in process project.  The goal is to be able to bundle up Scala software built with SBT for native packaging systems, like deb, rpm, homebrew, msi.


## Issues/Discussions

*  **Discussion/Questions**:
  If you wish to ask questions about the native packager, we're very active on [Stack Overflow](http://stackoverflow.com/questions/tagged/sbt).  You can either use the `sbt` tag or the `sbt-native-packager` tag.  They also have far better search support for working around issues.
* **Docs**:
   Our docs are [available online](http://scala-sbt.org/sbt-native-packager).  If you'd like to help improve the docs, they're part of this repository in the `src/sphinx` directory.
* **Issues/Feature Requests**:
  Finally, any bugs or features you find you need, please report to our [issue tracker](https://github.com/sbt/sbt-native-packager/issues/new).  Please check the [compatibility matrix](https://github.com/sbt/sbt-native-packager/wiki/Tested-On) to see if your system is able to produce the packages you want.

## Installation ##

Add the following to your `project/plugins.sbt` file:
    

    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.7.0-RC2")


## Documentation ##

There's a complete "getting started" guide and more detailed topics available at [the sbt-native-packager site](http://scala-sbt.org/sbt-native-packager).  


Please feel free to [contribute documentation](https://github.com/sbt/sbt-native-packager/tree/master/src/sphinx), or raise issues where you feel it may be lacking.


