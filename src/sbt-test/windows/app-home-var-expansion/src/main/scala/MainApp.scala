object MainApp {
  def main(args: Array[String]): Unit = {
    val config = sys.props("config.file") ensuring (_ ne null, "didn't pick up -Dconfig.file argument")
    print(config)
  }
}
