Generating files for the package
################################

Let's dynamically (in the build) construct some files that should be included in the package.


For the example, let's download a license file for our application and add it to the distribution. First,
let's create a task which will download a license file.  Add the following to build.sbt 

.. code-block:: scala

    val downloadLicense = taskKey[File]("Downloads the latest license file.")

    downloadLicense := {
      val location = target.value / "downloads" / "LICENSE"
      location.getParentFile.mkdirs()
      IO.download(url("http://www.schillmania.com/projects/soundmanager2/license.txt?txt"), location)
      location
    }

Now, we have a taks that will download the BSD license when run.  Note:  We assume that the license file is
something you host on your own website and keep up to date separately form the package.

Next, let's wire this license into the package.   The native package, by default, works with **mappings**.
In sbt, a **mappings** object is a grouping of files and relative locations, e.g ::

    /home/jsuereth/projects/example/src/universal/conf/app.config -> conf/app.config
    /home/jsuereth/projects/example/src/universal/conf/jvmopts -> conf/jvmopts

shows the mapping of the configuration files we set up :doc:`previously <customize>`.  We can directly
append files to the mappings rather than relying on the native packager to find things.  Let's add
the license in the root of the package we're creating.  Add the following to the ``build.sbt``

.. code-block:: scala

    mappings in Universal += downloadLicense.value -> "LICENSE"

This is appending a new mapping to those used for packaging.  In this case, we reference the file returned by
the ``downloadLicense`` task and put it in the root directory of the package, calling it ``LICENSE``.  We
can verify this by checking the ``stage`` task ::

    $ sbt stage
    $ ls target/universal/stage
    bin  conf  lib  LICENSE

You can see the license file is now included in the distribution.


With control over mappings, you can rework any aspect of the native packager defaults just by overriding
which files are used. However,  sometimes the defaults don't need to be completely replaced, just altered a bit.
