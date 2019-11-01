enablePlugins(AshStartScriptPlugin)

name := "simple-app"

version := "0.1.0"

TaskKey[Unit]("runCheck") := {
  val arguments = "-Xms64m -Xmx64m".split(" ")
  val cwd = (stagingDirectory in Universal).value / "bin"
  val cmd = cwd / packageName.value + ".sh"
  val memory = sys.process.Process(cmd, arguments).!!.trim
  assert(memory.toLong <= 64, "Runtime memory is bigger than 64m < " + memory + "m")
}
