import NativePackagerHelper._

enablePlugins(DebianPlugin)

name := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

// linuxPackageMappings in Debian += packageTemplateMapping("/var/run/debian")   // not work
// linuxPackageMappings in Debian += packageTemplateMapping("/var/run/debian")() // not work
linuxPackageMappings in Debian += packageTemplateMapping(Seq("/opt/test/other"): _*)()

linuxPackageMappings in Debian += {
  packageTemplateMapping("/opt/test/" + Keys.normalizedName.value)(target.value)
}

// Consider using mappings in Universal
linuxPackageMappings in Debian += packageDirectoryAndContentsMapping(
  (baseDirectory.value / "src" / "resources" / "conf") -> "/usr/share/conf"
)
