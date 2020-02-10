object MainApp extends App {  
  println(sys.props.collect { case (k, v) => s"$k=$v" } mkString "\n")
  println(args.mkString("|"))
}
