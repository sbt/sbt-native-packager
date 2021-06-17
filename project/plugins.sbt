addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.4.0")

// releasing
addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.5.7")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

// Scripted plugin needs to declare this as a dependency
libraryDependencies += "jline" % "jline" % "2.11"

// For code formatting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

// binary compatibility checks
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.7.0")
