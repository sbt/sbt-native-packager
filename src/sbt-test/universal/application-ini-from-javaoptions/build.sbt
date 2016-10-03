enablePlugins(JavaServerAppPackaging)

name := "simple-test"

version := "0.1.0"

javaOptions in Universal ++= Seq("-J-Xmx64m", "-J-Xms64m", "-Dproperty=true")

TaskKey[Unit]("check") := {
  val application = (target in Universal).value / "tmp" / "conf" / "application.ini"
  val content = IO.read(application)
  val options = (javaOptions in Linux).value
  options.foreach { opt =>
    assert(content.contains(opt), "Option [" + opt + "] is missing")
  }
}
