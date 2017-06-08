enablePlugins(JavaServerAppPackaging)

daemonUser in Debian := "root"

mainClass in Compile := Some("empty")

name := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

defaultLinuxLogsLocation := "/non-standard/log"

InputKey[Unit]("check-softlink") := {
  import complete.DefaultParsers._
  val args = spaceDelimited("<args>").parsed
  assert(args.size >= 2, "Usage: check-softlink link to target")
  val link = args(0)
  val target = args(args.size - 1)
  val absolutePath = ("readlink -m " + link).!!.trim
  assert(link != absolutePath, "Expected symbolic link '" + link + "' does not exist")
  assert(
    target == absolutePath,
    "Expected symbolic link '" + link + "' to point to '" +
      target + "', but instead it's '" + absolutePath + "'"
  )
}
