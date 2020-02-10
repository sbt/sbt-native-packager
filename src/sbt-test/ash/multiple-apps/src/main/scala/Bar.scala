import collection.JavaConverters._

object Bar extends App {
  println("Bar works")

  val jvmOptions = java.lang.management.ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.mkString(" ")
  val arguments = args.mkString(" ")

  println(jvmOptions + " " + arguments)
}
