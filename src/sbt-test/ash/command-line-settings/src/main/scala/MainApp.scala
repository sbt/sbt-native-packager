object MainApp {
  def main(args: Array[String]): Unit = {
    println(sys.props.collect { case (k, v) => s"$k=$v" } mkString "\n")
    println(args.mkString("|"))
  }
}
