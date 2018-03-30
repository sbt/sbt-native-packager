enablePlugins(JavaAppPackaging)

name := "simple-test"
version := "0.1.0"

// add some mappings
mappings in UniversalSrc := (mappings in Universal).value
mappings in UniversalDocs := (mappings in Universal).value
