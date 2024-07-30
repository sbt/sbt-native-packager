// Workaround to fix 'undefined setting error' :
// [error] Runtime reference to undefined setting:
// [error]   test-project-simple-build/sbt:scalafmtOnCompile from */*:onLoad ((com.lucidchart.sbt.scalafmt.ScalafmtSbtPlugin.globalSettings) ScalafmtSbtPlugin.scala:16)

addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.10")
