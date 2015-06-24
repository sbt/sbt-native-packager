enablePlugins(PlayScala, JDebPackaging)

name := "dtest"
version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"
packageSummary := "Test debian package"
packageDescription := """A fun package description of our software,
  with multiple lines."""

rpmVendor := "typesafe"
rpmLicense := Some("BSD")
rpmChangelogFile := Some("changelog.txt")

debianMaintainerScripts := {       
  Seq(
    file("src/debian/DEBIAN/postrm") -> "postrm",
  file("src/debian/DEBIAN/postinst") -> "postinst")
}
debianMakePostinstScript := {        
  Some(sourceDirectory.value / "src/debian/DEBIAN" / "override")
}    

// Setting some options
javaOptions in Universal ++= Seq(
  // JVM memory tuning
  "-J-Xmx1024m",
  "-J-Xms512m",

  // Since play uses separate pidfile we have to provide it with a proper path
  s"-Dpidfile.path=/var/run/${packageName.value}/play.pid",

  // Use separate configuration file for production environment
  s"-Dconfig.file=/usr/share/${packageName.value}/conf/production.conf",

  // Use separate logger configuration file for production environment
  s"-Dlogger.file=/usr/share/${packageName.value}/conf/production-logger.xml",

  // You may also want to include this setting if you use play evolutions
  "-DapplyEvolutions.default=true"
)

// Test stuff
TaskKey[Unit]("check-script") <<= (NativePackagerKeys.stagingDirectory in Universal, target in Debian, name, version, maintainer in Debian, streams) map {
 (dir, debTarget, name, version, author, streams) =>
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
  // Check replacement
  val prerm = debTarget / "DEBIAN" / "prerm"
  val prermOutput = Process("bash " + prerm.getAbsolutePath).!!
  val prermExpected = "removing ${{name}}-${{version}} from ${{author}}"
  assert(prermOutput contains prermExpected, s"Failed to correctly run the prerm script!.  Found [${prermOutput}] wanted [${prermExpected}]")
}
