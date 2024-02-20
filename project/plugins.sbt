addSbtPlugin("com.github.sbt" % "sbt-ghpages" % "0.8.0")
addSbtPlugin("com.github.sbt" % "sbt-site-sphinx" % "1.7.0")

// releasing
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.12")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

// Scripted plugin needs to declare this as a dependency
libraryDependencies += "jline" % "jline" % "2.14.6"

// For code formatting
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

// binary compatibility checks
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.3")

// for enterprise Artifactory compatibility
addSbtPlugin("com.scalawilliam.esbeetee" % "sbt-vspp" % "0.4.11")
