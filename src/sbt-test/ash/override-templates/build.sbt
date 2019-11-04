import scala.io.Source

enablePlugins(AshStartScriptPlugin)

name := "override-templates"

version := "0.1.0"

ashScriptTemplateLocation := baseDirectory.value / "custom-templates" / "custom-ash-template"
ashScriptReplacements := Seq("mainclass" -> "MainApp", "available-mainclasses" -> "BRAVO CHARLIE", "classpath" -> "foo.jar:bar.jar")

TaskKey[Unit]("runCheck") := {
  val cwd = (stagingDirectory in Universal).value / "bin"
  val source = scala.io.Source.fromFile(cwd / executableScriptName.value + ".sh")
  val contents = try source.getLines.mkString("\n") finally source.close()
  assert(
    contents contains "this is the custom ash template",
    "Ash template didn't contain the right text: \n" + contents
  )
  assert(contents.contains("MAINCLASS=MainApp"), "Template didn't contain MAINCLASS: \n" + contents)
  assert(contents.contains("AVAILIALBLE_MAINCLASSES=BRAVO CHARLIE"), "Template didn't contain AVAILIALBLE_MAINCLASSES: \n" + contents)
  assert(contents.contains("CLASSPATH=foo.jar:bar.jar"), "Template didn't contain CLASSPATH: \n" + contents)
}
