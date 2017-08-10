package test

// use dependency library
import com.typesafe.config._

object Test extends App {

  val config = ConfigFactory.load()

  println("SUCCESS!")

}
