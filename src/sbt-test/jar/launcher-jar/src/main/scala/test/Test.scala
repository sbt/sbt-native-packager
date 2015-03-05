package test

// use dependency library

import akka.actor._
import akka.routing.RoundRobinRouter

class PrintActor extends Actor{
  def receive = {
    case msg => println(msg)
  }
}

object Test extends App {
  val system = ActorSystem("testSystem")
  val router = system.actorOf(Props[PrintActor])
  router ! "SUCCESS!!"
  system.shutdown
}
