import com.typesafe.sbt.packager.Compat._

enablePlugins(JavaAppPackaging)

name := "windows-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "test-windows"

packageDescription := """Test Windows Batch."""

// debug out
val debugOutFile = file(".") / "debug_out.txt"

batScriptExtraDefines += "call :print_args %%* > " + debugOutFile.getAbsolutePath

batScriptExtraDefines += "goto print_args_end"

batScriptExtraDefines += ":print_args"

batScriptExtraDefines += "echo cmdcmdline=!cmdcmdline!"

batScriptExtraDefines += "echo *=%*"

batScriptExtraDefines += 1.to(9).map(i => "echo %%" + i + "=%" + i).mkString("\n")

batScriptExtraDefines += "echo _JAVA_OPTS=!_JAVA_OPTS!"

batScriptExtraDefines += "echo _APP_ARGS=!_APP_ARGS!"

batScriptExtraDefines += "exit /B"

batScriptExtraDefines += ":print_args_end"

TaskKey[Unit]("checkScript") := {
  val dir = (stagingDirectory in Universal).value
  import scala.sys.process._
  val fails = new StringBuilder()
  val script = dir / "bin" / (name.value + ".bat")
  val detailScript: File = {
    val d = dir / "bin" / "detail.bat"
    val out = new java.io.PrintWriter(d, "UTF-8")
    out.print(scala.io.Source.fromFile(script).mkString.replaceAll("@echo off", "echo on & prompt \\$g "))
    out.close
    d
  }
  def crlf2cr(txt: String) = txt.trim.replaceAll("\\\r\\\n", "\n")
  def checkOutput(testName: String,
                  args: Seq[String],
                  expected: String,
                  env: Map[String, String] = Map.empty,
                  expectedRC: Int = 0) = {
    val pr = new StringBuilder()
    val logger = ProcessLogger((o: String) => pr.append(o + "\n"), (e: String) => pr.append("error < " + e + "\n"))
    val cmd = Seq("cmd", "/c", script.getAbsolutePath) ++ args
    val result = sys.process.Process(cmd, None, env.toSeq: _*) ! logger
    if (result != expectedRC) {
      pr.append("error code: " + result + "\n")
    }
    val output = crlf2cr(pr.toString)
    if (result != expectedRC || output != expected.trim) {
      fails.append("\n---------------------------------\n")
      fails.append(testName)
      fails.append("\n---------------------------------\n")
      fails.append("Failed to correctly run the main script!.\n")
      fails.append("\"" + cmd.mkString("\" \"") + "\"\n")
      if (debugOutFile.exists) {
        fails.append(crlf2cr(scala.io.Source.fromFile(debugOutFile).mkString))
      }
      fails.append("\n--return code---------------------------\n")
      fails.append("\ngot: " + result + ", expected: " + expectedRC + "\n")
      fails.append("\n--expected----------------------------\n")
      fails.append(expected.trim + "\n")
      fails.append("\n--found-------------------------------\n")
      fails.append(crlf2cr(pr.toString) + "\n")
      fails.append("\n--detail-------------------------------\n")
      pr.clear
      sys.process.Process(Seq("cmd", "/c", detailScript.getAbsolutePath + " " + args), None, env.toSeq: _*) ! logger
      fails.append(crlf2cr(pr.toString) + "\n")
    }
    if (debugOutFile.exists) {
      debugOutFile.delete()
    }
  }

  checkOutput("normal argmument", Seq("OK"), "arg #0 is [OK]\nSUCCESS!")
  checkOutput("with -D", Seq("-Dtest.hoge=huga", "OK"), "arg #0 is [OK]\nproperty(test.hoge) is [huga]\nSUCCESS!")
  checkOutput(
    "with -J java-opt",
    Seq("-J-Xms6m", "OK"),
    "arg #0 is [OK]\nvmarg #0 is [-Xms6m]\nSUCCESS!",
    Map("show-vmargs" -> "true")
  )
  checkOutput(
    "complex",
    Seq("first", "-Dtest.hoge=huga", "-J-Xms6m", "-XX", "last"),
    "arg #0 is [first]\narg #1 is [-XX]\narg #2 is [last]\nproperty(test.hoge) is [huga]\nvmarg #0 is [-Dtest.hoge=huga]\nvmarg #1 is [-Xms6m]\nSUCCESS!",
    Map("show-vmargs" -> "true")
  )
  checkOutput(
    "include space",
    Seq("""-Dtest.hoge=C:\Program Files\Java""", """"C:\Program Files\Java""""),
    "arg #0 is [C:\\Program Files\\Java]\nproperty(test.hoge) is [C:\\Program Files\\Java]\nSUCCESS!"
  )
  checkOutput("include symbols on -D", Seq("-Dtest.hoge=\\[]!< >%"), "property(test.hoge) is [\\[]!< >%]\nSUCCESS!")
  checkOutput("include symbols on normal args", Seq("\"\\[]!< >%\""), "arg #0 is [\\[]!< >%]\nSUCCESS!")

  /* fails test because symbols '<' and '>' cannot be properly escaped during cmd execution
  checkOutput(
    "include symbols with double quote",
    Seq("-Dtest.huga=\"[]!<>%\""),
    "property(test.huga) is [[]!<>%]\nSUCCESS!"
  )
  */

  checkOutput(
    "include symbols with double quote2",
    Seq("-Dtest.hoge=\\[]!< >%", "\"\\[]!< >%\"", "-Dtest.huga=\\[]!<>%"),
    "arg #0 is [\\[]!< >%]\nproperty(test.hoge) is [\\[]!< >%]\nproperty(test.huga) is [\\[]!<>%]\nSUCCESS!"
  )

  // can't success include double-quote. arguments pass from Process(Seq("-Da=xx\"yy", "aa\"bb")) is parsed (%1="-Da", %2="xx\"yy aa\"bb") by cmd.exe ...
  //checkOutput("include space and double-quote",
  //  "-Dtest.hoge=aa\"bb xx\"yy",
  //  "arg #0 is [xx\"yy]\nproperty(test.hoge) is [aa\"bb]\nvmarg #0 is [-Dtest.hoge=aa\"bb]\nSUCCESS!")

  checkOutput("return-cord not 0", Seq("RC1"), "arg #0 is [RC1]\nFAILURE!", Map("return-code" -> "1"), 1)
  checkOutput("return-cord not 0 and 1", Seq("RC2"), "arg #0 is [RC2]\nFAILURE!", Map("return-code" -> "2"), 2)
  checkOutput("return-code negative", Seq("RC-1"), "arg #0 is [RC-1]\nFAILURE!", Map("return-code" -> "-1"), -1)
  assert(fails.toString == "", fails.toString)
}
