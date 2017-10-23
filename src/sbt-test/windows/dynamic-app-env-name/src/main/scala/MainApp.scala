object MainApp extends App {
  val config = sys.props("config.file") ensuring (_ ne null, "didn't pick up -Dconfig.file argument")
  print(config)
}
