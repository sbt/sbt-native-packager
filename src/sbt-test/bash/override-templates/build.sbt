import scala.io.Source

enablePlugins(JavaAppPackaging)

name := "override-templates"

version := "0.1.0"

bashScriptTemplateLocation := baseDirectory.value / "custom-templates" / "custom-bash-template"

batScriptTemplateLocation := baseDirectory.value / "custom-templates" / "custom-bat-template"

TaskKey[Unit]("run-check-bash") := {
  val cwd = (stagingDirectory in Universal).value
  val source =
    scala.io.Source.fromFile((cwd / "bin" / packageName.value).getAbsolutePath)
  val contents = try source.getLines mkString "\n"
  finally source.close()
  assert(contents contains "this is the custom bash template",
         "Bash template didn't contain the right text: \n" + contents)
}

TaskKey[Unit]("run-check-bat") := {
  val cwd = (stagingDirectory in Universal).value
  val batFilename = packageName.value + ".bat"
  val source =
    scala.io.Source.fromFile((cwd / "bin" / batFilename).getAbsolutePath)
  val contents = try source.getLines mkString "\n"
  finally source.close()
  assert(contents contains "this is the custom bat template",
         "Bat template didn't contain the right text: \n" + contents)
}
