import com.typesafe.sbt.packager.Keys.stagingDirectory

enablePlugins(JavaAppPackaging)

name := "script-help"

version := "0.1.0"

TaskKey[Unit]("runCheck") := {
  val cwd = (Universal / stagingDirectory).value
  val cmd = Seq((cwd / "bin" / packageName.value).getAbsolutePath, "-h")

  val buffer = new StringBuffer
  val code = sys.process.Process(cmd, cwd).run(scala.sys.process.BasicIO(false, buffer, None)).exitValue()
  assert(code == 1, "Exit code for -h was not 1: " + code)

  val output = buffer.toString.replaceAll("\n", "")

  val expectedHelpSamples = Seq(
    "-h | -help", "print this message",
    "-jvm-debug",
    "JAVA_OPTS",
    "special option"
  )

  assert(expectedHelpSamples.forall(output.contains(_)),
    s"Application did not print the correct help message: \n" + output)
}
