addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.5.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.0.0")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.6")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value

// Scripted plugin needs to declare this as a dependency
libraryDependencies += "jline" % "jline" % "2.11"

// For our bintray publishing
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

// For code formatting
//todo Uncomment in fut
//addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.10")
