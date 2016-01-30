enablePlugins(DebianPlugin)

enablePlugins(DebianArtifactoryDeployPlugin)

name := "debian-test"

version := "0.1.0"

maintainer := "Dmytro Aleksandrov <alkersan@gmail.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

debianArtifactoryUrl in Debian := "http://localhost:8081/artifactory"

debianArtifactoryCredentials in Debian := Some(Credentials("Artifactory", "localhost", "admin", "password"))

debianArtifactoryRepo in Debian := "apt"

debianArtifactoryPath in Debian := s"pool/${packageName.value}_${version.value}_${(packageArchitecture in Debian).value}.deb"

debianArtifactoryDistribution in Debian := Seq("trusty", "wily")

debianArtifactoryComponent in Debian := Seq("main", "contrib")

publish in Debian <<= (debianArtifactoryPublish in Debian)