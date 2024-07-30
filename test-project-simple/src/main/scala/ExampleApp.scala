package hello.world

object ExampleApp extends App {

  val memory = Runtime.getRuntime.maxMemory() / (1024L * 1024L)
  println(s"Memory $memory m")
  println(s"Args: ${args mkString " | "}")

  while (true) {
    println(s"[${System.currentTimeMillis()}] Hello, world!")
    Thread sleep 5000
  }

}
