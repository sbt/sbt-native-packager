//import NativePackagerKeys._

packageArchetype.java_application

name := "windows-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "test-windows"

packageDescription := """Test Windows MSI."""

wixProductId := "ce07be71-510d-414a-92d4-dff47631848a"

wixProductUpgradeId := "4552fb0e-e257-4dbd-9ecb-dba9dbacf424"

// debug out
val debugOutFile = file(".") / "debug_out.txt"

batScriptExtraDefines ++= Seq("call :print_args %%* > "+ debugOutFile.getAbsolutePath

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
  def crlf2cr(txt:String) = txt.trim.replaceAll("\\\r\\\n", "\n")
  def checkOutputEnv(env:Map[String,String], expected:String, args:String*) = {
    val pr = new StringBuilder()
    val logger = ProcessLogger((o: String) => pr.append(o+"\n"),(e: String) => pr.append("error < " + e+"\n"))
    val cmd = Seq("cmd", "/c", script.getAbsolutePath) ++ args
    val result = Process(cmd, None, env.toSeq:_*) ! logger
    if ( result != 0 ) {
      pr.append("error code: " + result+"\n")
    }
    val output = crlf2cr(pr.toString)
    if(output != expected.trim){
      fails.append("\n---------------------------------\n")
      fails.append("Failed to correctly run the main script!.\n")
      fails.append("\""+cmd.mkString("\" \"")+"\"\n")
      if(debugOutFile.exists){
        fails.append(crlf2cr(scala.io.Source.fromFile(debugOutFile).mkString))
      }
      fails.append("\n--expected----------------------------\n")
      fails.append(expected.trim+"\n")
      fails.append("\n--found-------------------------------\n")
      fails.append(crlf2cr(pr.toString)+"\n")
    }
    if(debugOutFile.exists){
      debugOutFile.delete()
    }
  }
  def checkOutput(expected:String, args:String*) = checkOutputEnv(Map.empty, expected, args:_*)
  checkOutput("arg #0 is [OK]\nSUCCESS!", "OK")
  checkOutput("arg #0 is [OK]\nproperty(test.hoge) is [huga]\nSUCCESS!", "-Dtest.hoge=\"huga\"", "OK")
  checkOutputEnv(Map("show-vmargs"->"true"), "arg #0 is [OK]\nvmarg #0 is [-Xms6m]\nSUCCESS!","-J-Xms6m", "OK")
  // include space
  checkOutput("arg #0 is [C:\\Program Files\\Java]\nproperty(test.hoge) is [C:\\Program Files\\Java]\nSUCCESS!", "-Dtest.hoge=C:\\Program Files\\Java", "C:\\Program Files\\Java")
  // include symbols
  checkOutput("arg #0 is [\\[]!< >%]\nproperty(test.hoge) is [\\[]!< >%]\nproperty(test.huga) is [\\[]!<>%]\nSUCCESS!",
    "\"-Dtest.hoge=\\[]!< >%\"", "\\[]!< >%", "-Dtest.huga=\"\\[]!<>%\"")
  // include space and double-quote is failed...
  // can't success include double-quote. arguments pass from Process(Seq("-Da=xx\"yy", "aa\"bb")) is parsed (%1="-Da", %2="xx\"yy aa\"bb") by cmd.exe ...
  //checkOutput("arg #0 is [xx\"yy]\nproperty(test.hoge) is [aa\"bb]\nvmarg #0 is [-Dtest.hoge=aa\"bb]\nSUCCESS!", "-Dtest.hoge=aa\"bb", "xx\"yy")
  assert(fails.toString == "", fails.toString)
}
