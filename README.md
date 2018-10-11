# SBT Native Packager #

[![Build Status](https://api.travis-ci.org/sbt/sbt-native-packager.png?branch=master)](https://travis-ci.org/sbt/sbt-native-packager)
[![Build status](https://ci.appveyor.com/api/projects/status/pbxd0untlcst4we7/branch/master?svg=true)](https://ci.appveyor.com/project/muuki88/sbt-native-packager/branch/master)
[![Codacy Badge](https://www.codacy.com/project/badge/0e9a7ec769c84e578f4550bf7da6bf05)](https://www.codacy.com/public/nepomukseiler/sbt-native-packager)
[ ![Download](https://api.bintray.com/packages/sbt/sbt-plugin-releases/sbt-native-packager/images/download.svg) ](https://bintray.com/sbt/sbt-plugin-releases/sbt-native-packager/_latestVersion)
[![Join the chat at https://gitter.im/sbt/sbt-native-packager](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/sbt/sbt-native-packager?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Documentation Status](https://readthedocs.org/projects/sbt-native-packager/badge/?version=latest)](http://sbt-native-packager.readthedocs.org/en/latest/?badge=latest)


![Native Packager Logo](src/sphinx/static/np_logo_full_horizontal_transparent.png)

## Goal ##

SBT native packager lets you build application packages in native formats. It offers
different archetypes for common configurations, such as simple Java apps or server applications.

## Issues/Discussions ##

*  **Discussion/Questions**:
  If you wish to ask questions about the native packager, we have a [mailinglist](https://groups.google.com/forum/#!forum/sbt-native-packager) and
  we're very active on [Stack Overflow](http://stackoverflow.com/questions/tagged/sbt). You can either use the `sbt` tag or the
  `sbt-native-packager` tag.  They also have far better search support for working around issues.
* **Docs**:
  [Our docs are available online](http://sbt-native-packager.readthedocs.org/en/latest/).  If you'd like to help improve the docs, they're part of this
  repository in the `src/sphinx` directory. [ScalaDocs](http://www.scala-sbt.org/sbt-native-packager/latest/api/#package) are also available.

  The old documentation can be found [here](http://www.scala-sbt.org/sbt-native-packager/)
* **Issues/Feature Requests**:
  Finally, any bugs or features you find you need, please report to our [issue tracker](https://github.com/sbt/sbt-native-packager/issues/new).
  Please check the [compatibility matrix](https://github.com/sbt/sbt-native-packager/wiki/Tested-On) to see if your system is able to
  produce the packages you want.

## Features ##

* Build [native packages][formats] for different systems
  * Universal `zip`,`tar.gz`, `xz` archives
  * `deb` and `rpm` packages for Debian/RHEL based systems
  * `dmg` for OSX
  * `msi` for Windows
  * `docker` images
* Provide archetypes for common use cases
  * [Java application][] with start scripts for Linux, OSX and Windows
  * [Java server application][] adds support for service managers:s
    * Systemd
    * Systemv
    * Upstart
* Java8 [jdkpackager][] wrapper
* Optional JDeb integration for cross-platform Debian builds
* Optional Spotify docker client integration

## Installation ##

Add the following to your `project/plugins.sbt` file:

```scala
// for autoplugins
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.10")
```

In your `build.sbt` enable the plugin you want. For example the
`JavaAppPackaging`.

```scala
enablePlugins(JavaAppPackaging)
```
Or if you need a server with autostart support

```scala
enablePlugins(JavaServerAppPackaging)
```

## Build ##

If you have enabled one of the archetypes (app or server),
you can build your application with

```bash
sbt <config-scope>:packageBin
```

### Examples

```bash
# universal zip
sbt universal:packageBin

# debian package
sbt debian:packageBin

# rpm package
sbt rpm:packageBin

# docker image
sbt docker:publishLocal
```

Read more in the specific [format documentation][formats] on how to configure and build your package.

## Documentation ##

There's a complete "getting started" guide and more detailed topics available at [the sbt-native-packager site](http://www.scala-sbt.org/sbt-native-packager/).

Please feel free to [contribute documentation](https://github.com/sbt/sbt-native-packager/tree/master/src/sphinx), or raise issues where you feel it may be lacking.

## Contributing ##

Please read the [contributing.md](CONTRIBUTING.md) on how to build and test native-packager.

## Related SBT Plugins ##

These are a list of plugins that either use sbt-native-packager, provide additional features
or provide a richer API for a single packaging format.

- [sbt-aether](https://github.com/arktekk/sbt-aether-deploy)
- [sbt-assembly](https://github.com/sbt/sbt-assembly)
- [sbt-bundle](https://github.com/sbt/sbt-bundle)
- [sbt-docker](https://github.com/marcuslonnberg/sbt-docker)
  - This is in addition to the built-in [Docker Plugin](http://www.scala-sbt.org/sbt-native-packager/formats/docker.html) from  sbt-native.  Both generate docker images. `sbt-docker` provides more customization abilities, while the `DockerPlugin` in this project  integrates more directly with predefined archetypes.
- [sbt-docker-containers](https://github.com/Dwolla/sbt-docker-containers) - enhances sbt-native-packager's docker functionality.
- [sbt-heroku](https://github.com/heroku/sbt-heroku)
- [sbt-newrelic](https://github.com/gilt/sbt-newrelic)
- [sbt-packager](https://github.com/en-japan/sbt-packer)
- [sbt-package-courier](https://github.com/alkersan/sbt-package-courier)
- [sbt-typesafe-conductr](https://github.com/sbt/sbt-conductr)


[formats]: http://www.scala-sbt.org/sbt-native-packager/gettingstarted.html#packaging-formats
[Java application]: http://www.scala-sbt.org/sbt-native-packager/archetypes/java_app/index.html
[Java server application]: http://www.scala-sbt.org/sbt-native-packager/archetypes/java_server/index.html
[My First Packaged Server Project guide]: http://www.scala-sbt.org/sbt-native-packager/GettingStartedServers/MyFirstProject.html
[jdkpackager]: http://www.scala-sbt.org/sbt-native-packager/formats/jdkpackager.html

## Maintainers ##

- Nepomuk Seiler (@muuki88)
- Alexey Kardapoltsev (@kardapoltsev)
- Derek Wickern (@dwickern)
- Felix Satyaputra (@fsat)

## Credits ##

- [Josh Suereth](https://twitter.com/jsuereth) for the initial developement
- [Sascha Rinaldi](http://www.imagelab.net/) for the native-packager logo
