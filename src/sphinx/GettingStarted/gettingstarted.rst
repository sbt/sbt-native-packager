Getting Started
===============

The sbt-native-packager is an sbt plugin.  Please follow the :ref:`Installation` instructions for how to set it up on a project.

The sbt-native-packager attempts to make building packages for different operating systems easier.  While it provides
some basic abstractions around packaging, it also allows you to dig down into the nuts and bolts of each platform as
neeeded to generate the best package possible.   


Here's the basic architecture of the plugin:

.. image:: https://docs.google.com/drawings/d/1ASOPHY8UUGLDHrYYXFWqfYOuQe5sBioX8GKkeN3Yvd0/pub?w=960&amp;h=720
   :height: 720 px
   :width: 960 px
   :alt: Architecture diagram.

When using the full power of the plugin, all of the packaging is driven from the ``mappings in Universal`` setting, which defines
what files will be included in the package.  These files are automatically moved around for the appropriate native packaging as needed.

We'll examine each level of  packaging.



Defining a new package
~~~~~~~~~~~~~~~~~~~~~~

To define a new package, after installing the plugin and ensuring the basic settings are on the project, start configuring your package contents
either using :ref:`Archetypes` or :ref:`Universal` hooks.  These will describe the appropriate way to begin packaging for your applciation.





