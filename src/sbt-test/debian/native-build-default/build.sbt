enablePlugins(DebianPlugin)

maintainer := "Maintainer <maintainer@example.com>"

packageDescription := "Description"

packageSummary := "Summary"

TaskKey[Unit]("checkDebCompression") := {
  val deb = target.value / s"${(name in Debian).value}_${(version in Debian).value}_all.deb"
  val output = sys.process.Process(Seq("ar", "-t", deb.toString)).lines
  assert(output.contains("data.tar"))
}
