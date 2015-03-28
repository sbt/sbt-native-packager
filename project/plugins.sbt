resolvers += Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.5.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "0.8.1")

libraryDependencies <+= (sbtVersion) { sv =>
  "org.scala-sbt" % "scripted-plugin" % sv
}

// Scripted plugin needs to declare this as a dependency
libraryDependencies += "jline" % "jline" % "2.11"

// For our bintray publishing
libraryDependencies += "net.databinder" %% "dispatch-http" % "0.8.10"

// For code formatting
addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")
