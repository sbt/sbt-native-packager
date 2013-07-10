package com.typesafe.sbt.packager.archetypes

object JavaAppBashScript {
  
  private[this] def bashTemplateSource =
    getClass.getResource("bash-template")
  private[this] def charset =
    java.nio.charset.Charset.forName("UTF-8")
  def generateScript(mainClass: String,
                     configFile: Option[String] = None): String = {
     val sb = new StringBuffer
     val defines: Seq[String] =
       (configFile map configFileDefine).toSeq ++
       Seq(mainClassDefine(mainClass))
     for(line <- sbt.IO.readLinesURL(bashTemplateSource, charset)) {
       if(line contains """${{template_declares}}""") {
         sb append (defines mkString "\n")
       } else {
         sb append line
         sb append "\n"
       }
     }
    sb.toString
  }
  
  def configFileDefine(configFile: String) =
    "declare -r script_conf_file=\"%s\"" format (configFile)
  
  def mainClassDefine(mainClass: String) = 
    "declare -r app_mainclass=\"%s\"\n" format (mainClass)
  
  
}