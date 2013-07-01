# SBT Native Packager #

This is a work in process project.  The goal is to be able to bundle up Scala software built with SBT for native packaging systems, like deb, rpm, homebrew, msi.


## Installation ##

Add the following to your `project/plugins.sbt` or `~/.sbt/plugins.sbt` file:
    
    resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
    
    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.6.0-symlink-3")

Then, in the project you wish to use the plugin, add the following settings:

    seq(packagerSettings:_*)

or

    settings(com.typesafe.sbt.SbtNativePackager.packagerSettings:_*)


## Usage ##

Using the sbt-native-packger plugin requires a bit of understanding of the underlying packaging mechanisms for each operating system it supports.  The [generated documentation](http://scala-sbt.org/sbt-native-packager) for the plugin is still a work in progress.


Here's an example excerpt for the native packaging of [sbt-launcher-packge](http://github.com/sbt/sbt-launcher-package) project:

    // GENERAL LINUX PACKAGING STUFFS
    maintainer := "Josh Suereth <joshua.suereth@typesafe.com>",
    packageSummary := "Simple Build Tool for Scala-driven builds",
    packageDescription := """This script provides a native way to run the Simple Build Tool,
  a build tool for Scala software, also called SBT.""",
    // Here we remove the jar file and launch lib from the symlinks:
    linuxPackageSymlinks <<= linuxPackageSymlinks map { links =>
      for { 
        link <- links
        if !(link.destination endsWith "sbt-launch-lib.bash")
        if !(link.destination endsWith "sbt-launch.jar")
      } yield link
    },
    // DEBIAN SPECIFIC    
    name in Debian <<= (sbtVersion) apply { (sv) => "sbt" /* + "-" + (sv split "[^\\d]" take 3 mkString ".")*/ },
    version in Debian <<= (version, sbtVersion) apply { (v, sv) =>
      val nums = (v split "[^\\d]")
      "%s-%s-build-%03d" format (sv, (nums.init mkString "."), nums.last.toInt + 1)
    },
    debianPackageDependencies in Debian ++= Seq("curl", "java2-runtime", "bash (>= 2.05a-11)"),
    debianPackageRecommends in Debian += "git",
    linuxPackageMappings in Debian <+= (sourceDirectory) map { bd =>
      (packageMapping(
        (bd / "debian/changelog") -> "/usr/share/doc/sbt/changelog.gz"
      ) withUser "root" withGroup "root" withPerms "0644" gzipped) asDocs()
    },
    
    // RPM SPECIFIC
    name in Rpm := "sbt",
    version in Rpm <<= sbtVersion apply { sv => (sv split "[^\\d]" filterNot (_.isEmpty) mkString ".") },
    rpmRelease := "1",
    rpmVendor := "typesafe",
    rpmUrl := Some("http://github.com/paulp/sbt-extras"),
    rpmLicense := Some("BSD"),
    
    
    // WINDOWS SPECIFIC
    name in Windows := "sbt",
    version in Windows <<= (sbtVersion) apply { sv =>
      (sv split "[^\\d]" filterNot (_.isEmpty)) match {
        case Array(major,minor,bugfix, _*) => Seq(major,minor,bugfix, "1") mkString "."
        case Array(major,minor) => Seq(major,minor,"0","1") mkString "."
        case Array(major) => Seq(major,"0","0","1") mkString "."
      }
    },
    maintainer in Windows := "Typesafe, Inc.",
    packageSummary in Windows := "Simple Build Tool",
    packageDescription in Windows := "THE reactive build tool.",
    wixProductId := "ce07be71-510d-414a-92d4-dff47631848a",
    wixProductUpgradeId := "4552fb0e-e257-4dbd-9ecb-dba9dbacf424",
    javacOptions := Seq("-source", "1.5", "-target", "1.5"),

    // Universal ZIP download install.
    name in Universal := "sbt"

The full build, including windows MSI generation, can be found [here](https://github.com/sbt/sbt-launcher-package/blob/full-packaging/project/packaging.scala).
