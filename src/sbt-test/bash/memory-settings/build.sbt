import com.typesafe.sbt.packager.Compat._

enablePlugins(JavaAppPackaging)

name := "simple-app"

version := "0.1.0"

(Universal / javaOptions) ++= Seq("-J-Xmx64m", "-J-Xms64m")

TaskKey[Unit]("jvmoptsCheck") := {
  val jvmopts = (Universal / stagingDirectory).value / "conf" / "application.ini"
  val options = IO.read(jvmopts)
  assert(options contains "-J-Xmx64m", "Options don't contain xmx setting:\n" + options)
  assert(options contains "-J-Xms64m", "Options don't contain xms setting:\n" + options)
}

TaskKey[Unit]("runCheck") := {
  val cwd = (Universal / stagingDirectory).value
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath)
  val memory = (sys.process.Process(cmd, cwd).!!).replaceAll("\n", "")
  assert(memory.toLong <= 64, "Runtime memory is bigger then 64m < " + memory + "m")
}
