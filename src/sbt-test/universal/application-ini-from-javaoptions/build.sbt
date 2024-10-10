enablePlugins(JavaServerAppPackaging)

name := "simple-test"

version := "0.1.0"

(Universal / javaOptions) ++= Seq("-J-Xmx64m", "-J-Xms64m", "-Dproperty=true")

TaskKey[Unit]("check") := {
  val application = (Universal / target).value / "tmp" / "conf" / "application.ini"
  val content = IO.read(application)
  val options = (Linux / javaOptions).value
  options.foreach { opt =>
    assert(content.contains(opt), "Option [" + opt + "] is missing")
  }
}
