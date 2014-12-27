enablePlugins(JavaAppPackaging)

name := "windows-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "test-windows"

packageDescription := """Test Windows Batch."""

// debug out
val debugOutFile = file(".") / "debug_out.txt"

batScriptExtraDefines += "call :print_args %%* > "+ debugOutFile.getAbsolutePath

batScriptExtraDefines += "goto print_args_end"

batScriptExtraDefines += ":print_args"

batScriptExtraDefines += "echo cmdcmdline=!cmdcmdline!"

batScriptExtraDefines += "echo *=%*"

batScriptExtraDefines += 1.to(9).map(i => "echo %%"+i+"=%"+i).mkString("\n")

batScriptExtraDefines += "echo _JAVA_OPTS=!_JAVA_OPTS!"

batScriptExtraDefines += "echo _APP_ARGS=!_APP_ARGS!"

batScriptExtraDefines += "exit /B"

batScriptExtraDefines += ":print_args_end"


TaskKey[Unit]("check-script") <<= (stagingDirectory in Universal, name, streams) map { (dir, name, streams) =>
  import scala.sys.process._ 
  val fails = new StringBuilder()
  val script = dir / "bin" / (name+".bat")
  val detailScript:File = {
    val d = dir / "bin" / "detail.bat"
    val out = new java.io.PrintWriter( d , "UTF-8")
    out.print( scala.io.Source.fromFile(script).mkString.replaceAll("@echo off","@echo on & prompt \\$g ") )
    out.close
    d
  }
  def crlf2cr(txt:String) = txt.trim.replaceAll("\\\r\\\n", "\n")
  def checkOutputEnv(env:Map[String,String], expectedRC: Int, expected:String, args:String*) = {
    val pr = new StringBuilder()
    val logger = ProcessLogger((o: String) => pr.append(o+"\n"),(e: String) => pr.append("error < " + e+"\n"))
    val cmd = Seq("cmd", "/c", script.getAbsolutePath) ++ args
    val result = Process(cmd, None, env.toSeq:_*) ! logger
    if ( result != expectedRC ) {
      pr.append("error code: " + result+"\n")
    }
    val output = crlf2cr(pr.toString)
    if(result != expectedRC || output != expected.trim){
      fails.append("\n---------------------------------\n")
      fails.append("Failed to correctly run the main script!.\n")
      fails.append("\""+cmd.mkString("\" \"")+"\"\n")
      if(debugOutFile.exists){
        fails.append(crlf2cr(scala.io.Source.fromFile(debugOutFile).mkString))
      }
      fails.append("\n--return code---------------------------\n")
      fails.append("\ngot: " + result+", expected: "+expectedRC+"\n")
      fails.append("\n--expected----------------------------\n")
      fails.append(expected.trim+"\n")
      fails.append("\n--found-------------------------------\n")
      fails.append(crlf2cr(pr.toString)+"\n")
      fails.append("\n--detail-------------------------------\n")
      pr.clear
      Process(Seq("cmd", "/c", detailScript.getAbsolutePath) ++ args, None, env.toSeq:_*) ! logger
      fails.append(crlf2cr(pr.toString)+"\n")
    }
    if(debugOutFile.exists){
      debugOutFile.delete()
    }
  }
  def checkOutput(expectedRC: Int, expected:String, args:String*) = checkOutputEnv(Map.empty, expectedRC, expected, args:_*)
  checkOutput(0, "arg #0 is [OK]\nSUCCESS!", "OK")
  checkOutput(0, "arg #0 is [OK]\nproperty(test.hoge) is [huga]\nSUCCESS!", "-Dtest.hoge=\"huga\"", "OK")
  checkOutputEnv(Map("show-vmargs"->"true"), 0, "arg #0 is [OK]\nvmarg #0 is [-Xms6m]\nSUCCESS!","-J-Xms6m", "OK")
  checkOutputEnv(Map("show-vmargs"->"true"), 0, "arg #0 is [first]\narg #1 is [-XX]\narg #2 is [last]\nproperty(test.hoge) is [huga]\nvmarg #0 is [-Dtest.hoge=huga]\nvmarg #1 is [-Xms6m]\nSUCCESS!",
    "first", "-Dtest.hoge=\"huga\"", "-J-Xms6m", "-XX", "last")
  // include space
  checkOutput(0, "arg #0 is [C:\\Program Files\\Java]\nproperty(test.hoge) is [C:\\Program Files\\Java]\nSUCCESS!",
    "-Dtest.hoge=C:\\Program Files\\Java", "C:\\Program Files\\Java")
  // split "include symbols"
  checkOutput(0, "property(test.hoge) is [\\[]!< >%]\nSUCCESS!", "\"-Dtest.hoge=\\[]!< >%\"")
  checkOutput(0, "arg #0 is [\\[]!< >%]\nSUCCESS!", "\\[]!< >%")
  checkOutput(0, "property(test.huga) is [\\[]!<>%]\nSUCCESS!", "-Dtest.huga=\"\\[]!<>%\"")
  // include symbols
  checkOutput(0, "arg #0 is [\\[]!< >%]\nproperty(test.hoge) is [\\[]!< >%]\nproperty(test.huga) is [\\[]!<>%]\nSUCCESS!",
    "\"-Dtest.hoge=\\[]!< >%\"", "\\[]!< >%", "-Dtest.huga=\"\\[]!<>%\"")
  // include space and double-quote is failed...
  // can't success include double-quote. arguments pass from Process(Seq("-Da=xx\"yy", "aa\"bb")) is parsed (%1="-Da", %2="xx\"yy aa\"bb") by cmd.exe ...
  //checkOutput(0, "arg #0 is [xx\"yy]\nproperty(test.hoge) is [aa\"bb]\nvmarg #0 is [-Dtest.hoge=aa\"bb]\nSUCCESS!", "-Dtest.hoge=aa\"bb", "xx\"yy")
  checkOutputEnv(Map("return-code"->"1"), 1, "arg #0 is [RC1]\nFAILURE!", "RC1")
  checkOutputEnv(Map("return-code"->"2"), 2, "arg #0 is [RC2]\nFAILURE!", "RC2")
  checkOutputEnv(Map("return-code"->"-1"), -1, "arg #0 is [RC-1]\nFAILURE!", "RC-1")
  assert(fails.toString == "", fails.toString)
}
