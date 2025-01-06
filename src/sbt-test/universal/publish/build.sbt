enablePlugins(UniversalDeployPlugin)

name := "simple-test"

version := "0.1.0"

lazy val testResolver =
  Resolver.file("test", file("test-repo"))(Patterns("[module]/[revision]/[module]-[revision].[ext]"))

// Workaround for ivy configuration bug
resolvers += testResolver
resolvers += Resolver.mavenLocal

// Workaround for overwriting packages at .m2 directory
ThisBuild / isSnapshot := true

Universal / publishTo := Some(testResolver)
