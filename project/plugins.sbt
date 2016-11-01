addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.5.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.0.0")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.0")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

libraryDependencies <+= (sbtVersion) { sv =>
  "org.scala-sbt" % "scripted-plugin" % sv
}

// Scripted plugin needs to declare this as a dependency
libraryDependencies += "jline" % "jline" % "2.11"

// For our bintray publishing
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

// For code formatting
addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "0.4.8")
