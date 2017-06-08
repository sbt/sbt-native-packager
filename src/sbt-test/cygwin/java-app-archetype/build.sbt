enablePlugins(JavaServerAppPackaging)

name := "windows-test"

version := "0.1.0"

TaskKey[Unit]("check-cygwin-script") := {
  val dir = (stagingDirectory in Universal).value
  // TODO - FIx our cygwin detection!
  val cygwinBash = file("C:\\cygwin\\bin\\bash.exe")
  if (!cygwinBash.exists)
    sys.error("Unable to find the default cygwin (c:\\cygwin) install for testing.")
  else {
    val script = dir / "bin" / name.value
    val PathParts = "([\\w])+\\:\\\\(.+)".r
    val PathParts(drive, path) = script.getAbsolutePath
    val cygdriveScriptPath = "/cygdrive/" + drive.toLowerCase + "/" + path.replaceAll("\\\\", "/")
    val pathEnv = "C:\\cygwin\\bin"
    val cmd = Seq(cygwinBash.getAbsolutePath, cygdriveScriptPath, "-d")
    val result =
      Process(cmd, Some(dir), "PATH" -> pathEnv) ! streams.value.log match {
        case 0 => ()
        case n =>
          sys.error("Failed to run script: " + cygdriveScriptPath + " error code: " + n)
      }
    val output = Process(cmd, Some(dir), "PATH" -> pathEnv).!!
    val expected = "SUCCESS!"
    assert(
      output contains expected,
      "Failed to correctly run the main script!.  Found [" + output + "] wanted [" + expected + "]"
    )
  }
}
