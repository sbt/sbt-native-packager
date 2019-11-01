import collection.JavaConverters._

object MainApp extends App {
  val jvmOptions = java.lang.management.ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.mkString(" ")
  val arguments = args.mkString(" ")

  println(jvmOptions + " " + arguments)

}
