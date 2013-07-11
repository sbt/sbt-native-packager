package com.typesafe.sbt.packager.archetypes

object JavaAppBatScript {
   private[this] def bashTemplateSource =
    getClass.getResource("bat-template")
  private[this] def charset =
    java.nio.charset.Charset.forName("UTF-8")
    
  def makeEnvFriendlyName(name: String): String =
    name.toUpperCase.replaceAll("\\W", "_")   
  
  def makeWindowsRelativeClasspath(cp: Seq[String]): String = {
     def cleanPath(path: String): String = path.replaceAll("/", "\\")
     def makeRelativePath(path: String): String =
       "%APP_LIB_DIR%\\" + cleanPath(path)
     cp map makeRelativePath mkString ":"
   }
    // TODO - Allow recursive replacements....
   def makeReplacements(
       name: String, 
       mainClass: String, 
       appClasspath: Seq[String] = Seq("*"),
       extras: Seq[(String,String)] = Nil): Seq[(String, String)] = {
     Seq(
         "APP_NAME" -> name,
         "APP_MAIN_CLASS" -> mainClass,
         "APP_ENV_NAME" -> makeEnvFriendlyName(name),
         "APP_CLASSPATH" -> makeWindowsRelativeClasspath(appClasspath)
     ) ++ extras
   }
    
   def generateScript(
       replacements: Seq[(String,String)]): String = {
     val sb = new StringBuffer
     for(line <- sbt.IO.readLinesURL(bashTemplateSource, charset)) {
       val fixed =
         replacements.foldLeft(line) { 
           case (line, (key, value)) =>
             line.replaceAll("@@"+key+"@@", java.util.regex.Matcher.quoteReplacement(value))
         }
       sb append fixed
       sb append "\r\n"
     }
    sb.toString
  }
}