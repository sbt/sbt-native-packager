# SBT Native Packager #

This is a work in process project.  The goal is to be able to bundle up Scala software built with SBT for native packaging systems, like deb, rpm, homebrew, msi.


## Installation ##

Add the following to your `project/plugins.sbt` or `~/.sbt/plugins.sbt` file:
    
    resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
    
    addSbtPlugin("com.typesafe" % "sbt-native-packager" % "0.2.0")

Then, in the project you wish to use the plugin, add the following settings:

    seq(packagerSettings:_*)

or

    settings(com.typesafe.packager.PackagerPlugin.packagerSettings:_*)


## Usage ##

Using the sbt-native-packger plugin requires a bit of understanding of the underlying packaging mechanisms for each operating system it supports.  The [generated documentation](http://scala-sbt.org/sbt-native-packager) for the plugin is still a work in progress.


Here's an example excerpt for the debian + rpm package of [sbt-extras](http://github.com/paulp/sbt-extras) project:

    // GENERAL LINUX PACKAGING STUFFS
    maintainer := "Josh Suereth <joshua.suereth@typesafe.com>",
    packageSummary := "Simple Build Tool for Scala-driven builds",
    packageDescription := """This script provides a native way to run the Simple Build Tool,
    a build tool for Scala software, also called SBT.""",
    linuxPackageMappings <+= (baseDirectory) map { bd =>
      (packageMapping((bd / "sbt") -> "/usr/bin/sbt")
       withUser "root" withGroup "root" withPerms "0755")
    },
    linuxPackageMappings <+= (sourceDirectory) map { bd =>
      (packageMapping(
        (bd / "linux" / "usr/share/man/man1/sbt.1") -> "/usr/share/man/man1/sbt.1.gz"
      ) withPerms "0644" gzipped) asDocs()
    },
    linuxPackageMappings <+= (sourceDirectory in Linux) map { bd =>
      packageMapping(
        (bd / "usr/share/doc/sbt/copyright") -> "/usr/share/doc/sbt/copyright"
      ) withPerms "0644" asDocs()
    },   
    linuxPackageMappings <+= (sourceDirectory in Linux) map { bd =>
      packageMapping(
        (bd / "usr/share/doc/sbt") -> "/usr/share/doc/sbt"
      ) asDocs()
    },
    linuxPackageMappings <+= (sourceDirectory in Linux) map { bd =>
      packageMapping(
        (bd / "etc/sbt") -> "/etc/sbt"
      ) withConfig()
    },
    linuxPackageMappings <+= (sourceDirectory in Linux) map { bd =>
      packageMapping(
        (bd / "etc/sbt/sbtopts") -> "/etc/sbt/sbtopts"
      ) withPerms "0644" withConfig("noreplace")
    },
    linuxPackageMappings <+= (sbtLaunchJar, sourceDirectory in Linux, sbtVersion) map { (jar, dir, v) =>
      packageMapping(dir -> "/usr/lib/sbt",
                     dir -> ("/usr/lib/sbt/" + v),
                     jar -> ("/usr/lib/sbt/"+v+"/sbt-launch.jar")) withPerms "0755"
    },
    // DEBIAN SPECIFIC    
    name in Debian := "sbt",
    version in Debian <<= (version, sbtVersion) apply { (v, sv) =>       
      sv + "-build-" + (v split "\\." map (_.toInt) dropWhile (_ == 0) map ("%02d" format _) mkString "")
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
    version in Rpm <<= sbtVersion.identity,
    rpmRelease := "1",
    rpmVendor := "typesafe",
    rpmUrl := Some("http://github.com/paulp/sbt-extras"),
    rpmLicense := Some("BSD"),


The full build, including windows MSI generation, can be found [here](https://github.com/sbt/sbt-launcher-package/blob/full-packaging/project/packaging.scala).
