addSbtPlugin("com.github.sbt" % "sbt-ghpages" % "0.9.0")
addSbtPlugin("com.github.sbt" % "sbt-site-sphinx" % "1.7.0")

// releasing
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.11.2")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

// Scripted plugin needs to declare this as a dependency
libraryDependencies += "jline" % "jline" % "2.14.6"

// For code formatting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.5")

// binary compatibility checks
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.4")
