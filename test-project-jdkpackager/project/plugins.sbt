lazy val packager =  ProjectRef(file("../.."), "sbt-native-packager")
dependsOn(packager)
