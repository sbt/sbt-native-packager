object MainApp extends App {
  val memory = Runtime.getRuntime.maxMemory() / (1024L * 1024L)
  print(memory)
}
