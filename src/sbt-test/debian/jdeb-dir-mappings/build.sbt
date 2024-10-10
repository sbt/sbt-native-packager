enablePlugins(JavaServerAppPackaging, JDebPackaging)

name := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

(Debian / debianPackageDependencies) ++= Seq("java2-runtime", "bash (>= 2.05a-11)")

(Debian / debianPackageRecommends) += "git"

(Debian / linuxPackageMappings) += packageDirectoryAndContentsMapping(
  (baseDirectory.value / "src" / "resources" / "conf") -> "/usr/share/conf"
)

(Debian / linuxPackageMappings) += packageDirectoryAndContentsMapping(
  (baseDirectory.value / "src" / "resources" / "empty") -> "/var/empty"
)

TaskKey[Unit]("checkDirMappings") := {
//  val tmpDir = java.nio.file.Files.createTempDirectory("jdeb")
  val extracted = file("/tmp/jdeb" + System.currentTimeMillis().toString)
  sys.process
    .Process(Seq("dpkg-deb", "-R", (target.value / "debian-test_0.1.0_all.deb").absolutePath, extracted.absolutePath))
    .!
  assert((extracted / "usr/share/conf/application.conf").exists(), "File application.conf not exists")
  assert((extracted / "usr/share/conf/log4j.properties").exists(), "File log4j.properties not exists")
  assert((extracted / "var/empty").exists(), "Empty dir not exists")
  extracted.delete()
  streams.value.log.success("Successfully tested control script")
  ()
}
