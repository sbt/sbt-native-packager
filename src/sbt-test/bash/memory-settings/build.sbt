enablePlugins(JavaAppPackaging)

name := "simple-app"

version := "0.1.0"

javaOptions in Universal ++= Seq(
  "-J-Xmx64m", "-J-Xms64m"
)

TaskKey[Unit]("jvmopts-check") := { 
  val jvmopts = (stagingDirectory in Universal).value / "conf" / "application.ini"
  val options = IO.read(jvmopts)
  assert(options contains "-J-Xmx64m", "Options don't contain xmx setting:\n" + options)
  assert(options contains "-J-Xms64m", "Options don't contain xms setting:\n" + options)
}

TaskKey[Unit]("run-check") := { 
  val cwd = (stagingDirectory in Universal).value
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath)
  val memory = (Process(cmd, cwd).!!).replaceAll("\n", "")
  assert(memory.toLong <= 64, "Runtime memory is bigger then 64m < " + memory + "m")
}

