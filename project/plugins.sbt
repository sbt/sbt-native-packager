// https://github.com/lightbend/mima/issues/422
resolvers += sbt.Resolver.bintrayIvyRepo("typesafe", "sbt-plugins")

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.3.3")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13")
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.1.1")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.1")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

// Scripted plugin needs to declare this as a dependency
libraryDependencies += "jline" % "jline" % "2.11"

// For our bintray publishing
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.4")

// For code formatting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

// binary compatibility checks
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.7.0")
