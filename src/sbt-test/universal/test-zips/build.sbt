enablePlugins(JavaAppPackaging)

name := "simple-test"
version := "0.1.0"

// add some mappings
UniversalSrc / mappings := (Universal / mappings).value
UniversalDocs / mappings := (Universal / mappings).value
