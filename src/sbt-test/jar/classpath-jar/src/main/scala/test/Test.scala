package test

// use dependency library

import akka.actor._
import akka.pattern._
import akka.routing.RoundRobinRouter
import akka.util.Timeout
import scala.concurrent.duration._

class PrintActor extends Actor{
  def receive = {
    case msg =>
      println(msg)
      context.sender ! "ok"
  }
}

object Test extends App {
  implicit val timeout = Timeout(3 seconds)

  val system = ActorSystem("testSystem")
  import system.dispatcher

  val router = system.actorOf(Props[PrintActor])
  (router ? "SUCCESS!!").foreach { _ =>
    system.shutdown
  }
}
