import java.util.concurrent._

object ExampleApp extends App {

  val executorService = Executors newFixedThreadPool 2

  val memory = Runtime.getRuntime.maxMemory() / (1024L * 1024L)
  println(s"Memory $memory m")
  println(s"Args: ${args mkString " | "}")

  while (true) {
    for (i <- 0 to 2) executorService execute HelloWorld(i)
    Thread sleep 5000
  }

}

case class HelloWorld(i: Int) extends Runnable {
  def run() {
    println(s"[$i] Hello, world!")
  }
}