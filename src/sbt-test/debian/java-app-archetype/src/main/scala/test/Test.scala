package test

object Test extends App {
  Option(sys.props("result.string")) match {
    case Some(value) => println(value)
    case _           => println("SUCCESS!")
  }

}
