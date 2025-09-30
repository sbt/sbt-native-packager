import com.typesafe.sbt.packager.Keys.stagingDirectory
import com.typesafe.sbt.packager.Compat._

enablePlugins(JavaAppPackaging)

name := "windows-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "test-windows"

packageDescription := """Test Windows MSI."""

wixProductId := "ce07be71-510d-414a-92d4-dff47631848a"

wixProductUpgradeId := "4552fb0e-e257-4dbd-9ecb-dba9dbacf424"

TaskKey[Unit]("checkScript") := {
  val script = (Universal / stagingDirectory).value / "bin" / (name.value + ".bat")
  val cmd = Seq("cmd", "/c", script.getAbsolutePath)
  val result =
    sys.process.Process(cmd) ! streams.value.log match {
      case 0 => ()
      case n =>
        sys.error("Failed to run script: " + script.getAbsolutePath + " error code: " + n)
    }
  val output = sys.process.Process(cmd).!!
  val expected = "SUCCESS!"
  assert(
    output contains expected,
    "Failed to correctly run the main script!.  Found [" + output + "] wanted [" + expected + "]"
  )
}
