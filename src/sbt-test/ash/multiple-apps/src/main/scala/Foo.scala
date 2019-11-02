import collection.JavaConverters._

object Foo extends App {
  println("Foo works")

  val jvmOptions = java.lang.management.ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.mkString(" ")
  val arguments = args.mkString(" ")

  println(jvmOptions + " " + arguments)
}
