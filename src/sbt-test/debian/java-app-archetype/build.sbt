import NativePackagerKeys._

packageArchetype.java_application

name := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

debianPackageDependencies in Debian ++= Seq("java2-runtime", "bash (>= 2.05a-11)")

debianPackageRecommends in Debian += "git"


TaskKey[Unit]("check-script") <<= (stagingDirectory in Universal, name, streams) map { (dir, name, streams) =>
  val script = dir / "bin" / name
  val cmd = "bash " + script.getAbsolutePath
  val result =
    Process(cmd) ! streams.log match {
      case 0 => ()
      case n => sys.error("Failed to run script: " + script.getAbsolutePath + " error code: " + n)
    }
  val output = Process("bash " + script.getAbsolutePath).!!
  val expected = "SUCCESS!"
  assert(output contains expected, "Failed to correctly run the main script!.  Found ["+output+"] wanted ["+expected+"]")
}
