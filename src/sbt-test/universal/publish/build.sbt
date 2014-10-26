enablePlugins(UniversalPlugin)

name := "simple-test"

version := "0.1.0"

// Workarund for ivy configuration bug
resolvers += (publishTo in Universal).value.get

publishTo in Universal := Some(Resolver.file("test", file("test-repo"))(Patterns("[module]/[revision]/[module]-[revision].[ext]")))
