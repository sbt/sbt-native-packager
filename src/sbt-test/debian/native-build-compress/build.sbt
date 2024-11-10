name := "foo"

enablePlugins(DebianPlugin)

Debian / debianNativeBuildOptions := Nil

maintainer := "Maintainer <maintainer@example.com>"

packageDescription := "Description"

packageSummary := "Summary"

TaskKey[Unit]("checkDebCompression") := {
  val deb = target.value / s"${(Debian / name).value}_${(Debian / version).value}_all.deb"
  val output = sys.process.Process(Seq("ar", "-t", deb.toString)).lines
  assert(output.exists(_.startsWith("data.tar."))) // exact extension varies by dpkg-deb version
}
