resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.8")

lazy val root = Project("plugins", file(".")) dependsOn(packager)

lazy val packager = file("..").getAbsoluteFile.toURI

