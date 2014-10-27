package test

import scala.collection.JavaConversions._

object Test extends App {
  override def main(args: Array[String]): Unit = {
	for((x,i) <- args.zipWithIndex) println("arg #" + i + " is [" + x + "]")
  	for((k,v) <- System.getProperties if k.startsWith("test.")) println("property(" + k + ") is [" + v + "]")
  	if(System.getenv("show-vmargs") == "true"){
  	  for((x,i) <- java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().zipWithIndex){
	    println("vmarg #" + i + " is [" + x + "]")
	  }
	}
    println("SUCCESS!")
  }
}
