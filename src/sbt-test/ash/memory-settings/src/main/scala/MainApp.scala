object MainApp {
  def main(args: Array[String]): Unit = {
    val memory = Runtime.getRuntime.maxMemory() / (1024L * 1024L)
    print(memory)
  }
}
