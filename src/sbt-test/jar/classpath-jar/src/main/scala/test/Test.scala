package test

// use dependency library
import com.typesafe.config._

object Test {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()

    println("SUCCESS!")
  }

}
