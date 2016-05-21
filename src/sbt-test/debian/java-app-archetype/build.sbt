enablePlugins(JavaAppPackaging)

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
  System.out.synchronized {
    System.err.println("---SCRIPT---")
    val scriptContents = IO.read(script)
    System.err.println(scriptContents)
    System.err.println("---END SCRIPT---")
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
  val expected2 = "Something with spaces"
  val output2 = Process(Seq("bash", script.getAbsolutePath, "-Dresult.string="+expected2)).!!
  assert(output2 contains expected2, "Failed to correctly run the main script with spaced java args!.  Found ["+output2+"] wanted ["+expected2+"]")
}
