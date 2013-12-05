# SBT Native Packager #

This is a work in process project.  The goal is to be able to bundle up Scala software built with SBT for native packaging systems, like deb, rpm, homebrew, msi.


## Issues/Discussions

*  **Discussion/Questions**:
  If you wish to ask questions about the native packager, we're very active on [Stack Overflow](http://stackoverflow.com/questions/tagged/sbt).  You can either use the `sbt` tag or the `sbt-native-packager` tag.  They also have far better search support for working around issues.
* **Docs**:
   Our docs are [available online](http://scala-sbt.org/sbt-native-packager).  If you'd like to help improve the docs, they're part of this repository in the `src/sphinx` directory.
* **Issues/Feature Requests**:
  Finally, any bugs or features you find you need, please report to our [issue tracker](https://github.com/sbt/sbt-native-packager/issues/new).

## Installation ##

Add the following to your `project/plugins.sbt` file:
    

    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.6.4")


Then, in the project you wish to use the plugin, You need to select what kind of project you are packaging:

### Java Application ###

If you are packaging a Java Application, this implies that you have *one* main method defined in the project.  The
native packager will generate two scrips to run your application (one for 'nix [bash] and one for windows [bat]). The
generic layout of your application will be:

     <installation-dir>
        bin/
           <app>             <- bash script
           <app>.bat         <- windows script
        lib/
           *.jar             <- binaries

When mapping to debian or RPM, the packager will create symlinks in /usr/bin to the installation directory of your
program.   If you include a `conf/` directory with configuration, this will show up as a symlink under `/etc/<app>/`.
On windows, the directory structure remains unchanged, however the MSI will include a hook to automatically add
the `bin/` directory to the windows PATH.

Here's what to add to your `build.sbt`:

    packageArchetype.java_application

If you'd like to add additional files to the installation dir, simply add them to the universal mappings:

    import com.typesafe.sbt.SbtNativePackager.Universal
    
    mappings in Universal += {
      file("my/local/conffile") -> "conf/my.conf"
    }

The above adds a configuration file from the local project at `my/local/conffile` into the installation directory
at `conf/my.conf`.


### Java Server Application (Experimental)  ###

If you are packaging a server, the configuration will be similar to a vanilla Java Application, except that the native
packager will include service hooks inside the MSI, DEB and RPM.   For DEB, these hooks will use upstart.  For RPM,
they will use init.d and for MSIs, there will be windows service hooks.

*Right now there is no active release with complete Java Server hooks. You should be able to start experimenting with Java server hooks using debian packaging and the upstart system.*

Here's what to add to your `build.sbt`:

    packageArchetype.java_server


### By-hand packaging ###

If you'd like to wire all of your packages by hand, use the minmal set of configuration provided.  In your
`build.sbt` enter the following:

    packagerSettings

or to a `Project` instantiation in `build.sbt`/`project/Build.scala`:

    settings(com.typesafe.sbt.SbtNativePackager.packagerSettings:_*)
    
If you use this configuration, you must fill out the `mappings in Universal`, `mappings in Windows`,
`linuxPackageMappings` and `wixXml` settings yourself.


## Usage ##

Once you've configured your packaging how you like it, you can run the following commands:

* `stage` - Creates an universal distribution under the `target/universal/stage` directory
* `universal:package-zip-tarball` - Creates an universal `.tgz` distribution.
* `universal:package-xz-tarball` - Creates an universal `txz` distribution.  Note: xz sucks cpu like no other.
* `universal:package-bin` - Creates an universal `zip` distribution
* `windows:package-bin` - Creates a Windows `msi` file.
* `windows:package-msi` - Creates a Windows `msi` file.
* `debian:package-bin` - Creates a Debian `deb` file.
* `rpm:package-bin` - Creates a Red Hat `rpm` file.


### Publishing to bintray ###

[Bintray](bintray.com) has support for publishing RPM + DEB files into shared repositories.  We can do this from sbt
using the sbt-native-packager.  TODO - outline details once we have them fleshed out.

A more complex project, which bundles the sbt project, can be found [here](https://github.com/sbt/sbt-launcher-package/blob/full-packaging/project/packaging.scala).
