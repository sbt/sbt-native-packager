.. _akka-app-plugin:

Akka Microkernel
################

.. danger::
    The **Akka Microkernel Archetype** is deprecated in favor of the more
    general and better maintained :ref:`java-app-plugin`.


An Akka microkernel application is similar to a Java Command Line application. Instead of running the classic ``mainClass``,
an Akka microkernel application instantiates and runs a subclass of
`Bootable <https://github.com/akka/akka/blob/master/akka-kernel/src/main/scala/akka/kernel/Main.scala>`_ . A minimal example
could look like this

.. code-block:: scala

   class HelloKernel extends Bootable {
     val system = ActorSystem("hellokernel")

     def startup = {
       // HelloActor and Start case object must of course be defined
       system.actorOf(Props[HelloActor]) ! Start
     }

     def shutdown = {
       system.terminate()
     }
   }

The *bash/bat* script that starts up the Akka application is copied from the Akka distribution.

To use this archetype in your build, add the following to your ``build.sbt``:

.. code-block:: scala

   packageArchetype.akka_application

   name := "A-package-friendly-name"

   mainClass in Compile := Some("HelloKernel")

For more information take a look at the akka docs

* `Akka microkernel <http://doc.akka.io/docs/akka/snapshot/scala/microkernel.html>`_
* `akka.kernel.Main source <https://github.com/akka/akka/blob/master/akka-kernel/src/main/scala/akka/kernel/Main.scala>`_
* `akka.kernel.Bootable docs <http://doc.akka.io/api/akka/snapshot/index.html#akka.kernel.Bootable>`_
