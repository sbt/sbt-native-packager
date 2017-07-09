import com.typesafe.sbt.packager.Compat._

enablePlugins(JavaAppPackaging)

name := "test-project"
version := "0.1.0"

TaskKey[Unit]("check-no-explicit-main") := {
  val cwd = (stagingDirectory in Universal).value

  // check MainApp
  val cmd = Seq((cwd / "bin" / "main-app").getAbsolutePath)
  val output = Process(cmd, cwd).!!.replaceAll("\n", "")
  assert(output == "MainApp", s"Output wasn't 'MainApp', but '$output'")

  // check SecondApp
  val cmdSecond = Seq((cwd / "bin" / "second-app").getAbsolutePath)
  val outputSecond = Process(cmdSecond, cwd).!!.replaceAll("\n", "")
  assert(outputSecond == "SecondApp", s"Output wasn't 'SecondApp': '$outputSecond'")
}

TaskKey[Unit]("check-explicit-main") := {
  val cwd = (stagingDirectory in Universal).value

  // check default start script
  val cmd = Seq((cwd / "bin" / executableScriptName.value).getAbsolutePath)
  val output = Process(cmd, cwd).!!.replaceAll("\n", "")
  assert(output == "MainApp", s"Output wasn't 'MainApp', but '$output'")

  // check SecondApp
  val cmdSecond = Seq((cwd / "bin" / "second-app").getAbsolutePath)
  val outputSecond = Process(cmdSecond, cwd).!!.replaceAll("\n", "")
  assert(outputSecond == "SecondApp", s"Output wasn't 'SecondApp', but '$outputSecond'")
}
