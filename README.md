# SBT Native Packager #

This is a work in process project.  The goal is to be able to bundle up Scala software built with SBT for native packaging systems, like deb, rpm, homebrew, msi.


## Installation ##

Add the following to your `project/plugins.sbt` or `~/.sbt/plugins.sbt` file:
    
    resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
    
    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.6.1")

Then, in the project you wish to use the plugin, add the following settings:

    seq(packagerSettings:_*)

or

    settings(com.typesafe.sbt.SbtNativePackager.packagerSettings:_*)


## Usage ##

Using the sbt-native-packger plugin requires a bit of understanding of the underlying packaging mechanisms for each operating system it supports.  The [generated documentation](http://scala-sbt.org/sbt-native-packager) for the plugin is still a work in progress.


Here's an example using the java_application archetype to create native packaging:

    packageArchetype.java_application

    name := "A-package-friendly-name"
    
    packageSummary in Linux := "The name you want displayed in package summaries"

    packageSummary in Windows := "The name you want displayed in Add/Remove Programs"

    packageDescription := " A description of your project"

    maintainer in Windows := "Company"
    
    maintainer in Debian := "Your Name <your@email.com>"

    wixProductId := "ce07be71-510d-414a-92d4-dff47631848a"

    wixProductUpgradeId := "4552fb0e-e257-4dbd-9ecb-dba9dbacf424"

A more complex project, which bundles the sbt project, can be found [here](https://github.com/sbt/sbt-launcher-package/blob/full-packaging/project/packaging.scala).
