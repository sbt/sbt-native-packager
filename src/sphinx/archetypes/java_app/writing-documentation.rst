Writing Documentation
#####################

There are many ways to document your projects, and many ways to expose them.  While the native packager places
no limit on WHAT is included in a package, there are some things which receive special treatment.

Specifically: linux man pages.


To create a linux man page for the application, let's create a ``src/linux/usr/share/man/man1/example-cli.1`` file ::


    .\" Process this file with
    .\" groff -man -Tascii example-cli.1
    .\"
    .TH EXAMPLE_CLI 1 "NOVEMBER 2011" Linux "User Manuals"
    .SH NAME
    example-cli \- Example CLI
    .SH SYNOPSIS
    .B example-cli [-h]

Notice the location of the file.  Any file under ``src/linux`` is automatically included,
relative to ``/``, in linux packages (deb, rpm).  That means the man file will **not** appear
in the universal package (confusing linux users).  

Now that the man page is created, we can use a few tasks provided to view it in sbt.  Let's look in the sbt console ::

    $ sbt
    > generateManPages
    [info] Generated man page for[/home/jsuereth/projects/sbt/sbt-native-packager/tutorial-example/src/linux/usr/share/man/man1/example-cli.1] =
    [info] EXAMPLE_CLI(1)                   User Manuals                   EXAMPLE_CLI(1)
    [info] 
    [info] 
    [info] 
    [info] NAME
    [info]        example-cli - Example CLI
    [info] 
    [info] SYNOPSIS
    [info]        example-cli [-h]
    [info] 
    [info] 
    [info] 
    [info] Linux                            NOVEMBER 2011                  EXAMPLE_CLI(1)


We can use this task to work on the man pages and ensure they'll look OK.  You can also directly use ``groff`` to view changes in 
your man pages.

In addition to providing the means to view the man page, the native packager will also automatically ``gzip`` man pages for the
distribution.  The resulting man page is stored in ``/usr/share/man/man1/example-cli.1.gz`` in linux distributions.


TODO - A bit more on other documentation methods.


That's the end for the getting started guide for Java Applications!  Feel free to read the guide on 
:doc:`Java Servers </archetypes/java_server/index>`, which offers a few differences in how configuration
is done for packaging to underlying systems.
