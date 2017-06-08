enablePlugins(JavaServerAppPackaging, UpstartPlugin)

mainClass in Compile := Some("empty")

name := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

daemonUser in Linux := "root"

daemonGroup in Linux := "root"

packageDescription := """A fun package description of our software,
  with multiple lines."""

// add this to override all preexisting settings
import DebianConstants._
maintainerScripts in Debian := maintainerScriptsFromDirectory(
  sourceDirectory.value / DebianSource / DebianMaintainerScripts,
  Seq(Preinst, Postinst, Prerm, Postrm)
)

TaskKey[Unit]("check-control-files") := {
  val debian = target.value / "debian-test-0.1.0" / "DEBIAN"
  val postinst = IO.read(debian / "postinst")
  val preinst = IO.read(debian / "preinst")
  val postrm = IO.read(debian / "postrm")
  val prerm = IO.read(debian / "prerm")
  // This is a fragile test
  // echo 'custom postinst ${{app_name}} ${{chdir}} ${{daemonUser}} ${{daemonGroup}}'
  assert(
    postinst equals "echo 'custom postinst debian-test /usr/share/debian-test root root'\n",
    "Wrong postinst:\n" + postinst
  )
  assert(preinst equals "echo 'custom preinst'\n", "Wrong preinst:\n" + preinst)
  assert(postrm equals "echo 'custom postrm'\n", "Wrong postrm:\n" + postrm)
  assert(prerm equals "echo 'custom prerm'\n", "Wrong prerm:\n" + prerm)
  ()
}
