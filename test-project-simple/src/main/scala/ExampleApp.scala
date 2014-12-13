import java.util.concurrent._

object ExampleApp extends App {

  val executorService = Executors newFixedThreadPool 2

  while (true) {
    for(i <- 0 to 5) executorService execute HelloWorld(i)
    Thread sleep 5000
  }

}

case class HelloWorld(i: Int) extends Runnable {
  def run() {
    println(s"[$i] Hello, world!")
  }
}