import NativePackagerKeys._

packageArchetype.java_server

name := "dtest"

version := "0.1.0"

val buildLoc = (file(".").getAbsoluteFile.getParentFile)

val dtestProj = ProjectRef(buildLoc, "np")

version in dtestProj := "0.2.0"


maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

debianPackageDependencies in Debian ++= Seq("java2-runtime", "bash (>= 2.05a-11)")

debianPackageRecommends in Debian += "git"


TaskKey[Unit]("check-script") <<= (NativePackagerKeys.stagingDirectory in Universal, name, streams) map { (dir, name, streams) =>
  val script = dir / "bin" / name
  System.out.synchronized {
    System.err.println("---SCIRPT---")
    val scriptContents = IO.read(script)
    System.err.println(scriptContents)
    System.err.println("---END SCIRPT---")
    for(file <- (dir.***).get)
      System.err.println("\t"+file)
  }
  val cmd = "bash " + script.getAbsolutePath + " -d"
  val result =
    Process(cmd) ! streams.log match {
      case 0 => ()
      case n => sys.error("Failed to run script: " + script.getAbsolutePath + " error code: " + n)
    }
  val output = Process("bash " + script.getAbsolutePath).!!
  val expected = "SUCCESS!"
  assert(output contains expected, "Failed to correctly run the main script!.  Found ["+output+"] wanted ["+expected+"]")
}
