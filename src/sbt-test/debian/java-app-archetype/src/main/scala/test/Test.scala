package test

object Test {
  def main(args: Array[String]): Unit =
    Option(sys.props("result.string")) match {
      case Some(value) => println(value)
      case _           => println("SUCCESS!")
    }

}
