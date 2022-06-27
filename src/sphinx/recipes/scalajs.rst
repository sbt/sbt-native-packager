.. _scalajs:

Scala JS packaging
==================

.. warning:: This is no official scala js doc, but created from the native-packager community.
    See `issue-699`_.

Package webjars and scalajs resources
-------------------------------------

In order to package all assets correctly, add this to your project

.. code-block:: scala

    Runtime / managedClasspath += (Assets / previewJVM / packageBin).value


.. _issue-699: https://github.com/sbt/sbt-native-packager/issues/699
