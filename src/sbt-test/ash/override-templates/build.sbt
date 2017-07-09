import scala.io.Source

enablePlugins(AshScriptPlugin)

name := "override-templates"

version := "0.1.0"

bashScriptTemplateLocation := baseDirectory.value / "custom-templates" / "custom-ash-template"

TaskKey[Unit]("runCheckAsh") := {
  val cwd = (stagingDirectory in Universal).value
  val source =
    scala.io.Source.fromFile((cwd / "bin" / packageName.value).getAbsolutePath)
  val contents = try source.getLines mkString "\n"
  finally source.close()
  assert(
    contents contains "this is the custom bash template",
    "Bash template didn't contain the right text: \n" + contents
  )
  assert(contents contains "app_mainclass=", "Template didn't contain app_mainclass: \n" + contents)
  assert(!(contents contains "declare"), "Template didn't contains declare: \n" + contents)
}
