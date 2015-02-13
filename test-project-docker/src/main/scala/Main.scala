import java.nio.file._
import scala.util._

object Main extends App {
  println("Hello world")
  
  val path = Paths get "/data/test01"
  val result = Try(Files createFile path)
  
  println(result)
}
