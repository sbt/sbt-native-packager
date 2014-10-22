import NativePackagerKeys._

enablePlugins(JavaAppPackaging)

name := "windows-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "test-windows"

packageDescription := """Test Windows MSI."""

wixProductId := "ce07be71-510d-414a-92d4-dff47631848a"

wixProductUpgradeId := "4552fb0e-e257-4dbd-9ecb-dba9dbacf424"


TaskKey[Unit]("check-script") <<= (stagingDirectory in Universal, name, streams) map { (dir, name, streams) =>
  val script = dir / "bin" / (name+".bat")
  val cmd = Seq("cmd", "/c", script.getAbsolutePath)
  val result =
    Process(cmd) ! streams.log match {
      case 0 => ()
      case n => sys.error("Failed to run script: " + script.getAbsolutePath + " error code: " + n)
    }
  val output = Process(cmd).!!
  val expected = "SUCCESS!"
  assert(output contains expected, "Failed to correctly run the main script!.  Found ["+output+"] wanted ["+expected+"]")
}
