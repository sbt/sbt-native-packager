libraryDependencies += "com.github.xuwei-k" %% "scala-version-from-sbt-version" % "0.1.0"

addSbtPlugin("com.github.sbt" % "sbt-ghpages" % "0.10.0")
addSbtPlugin("com.github.sbt" % "sbt-site-sphinx" % "1.8.0")

// releasing
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.12.1")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

// Scripted plugin needs to declare this as a dependency
libraryDependencies += "jline" % "jline" % "2.14.6"

// For code formatting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.2")

// binary compatibility checks
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.2.1")
