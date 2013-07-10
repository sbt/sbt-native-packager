package com.typesafe.sbt.packager.archetypes

object JavaAppBatScript {
   private[this] def bashTemplateSource =
    getClass.getResource("bat-template")
  private[this] def charset =
    java.nio.charset.Charset.forName("UTF-8")
    
  def makeEnvFriendlyName(name: String): String =
    name.toUpperCase.replaceAll("\\W", "_")   
    
   def generateScript(
       name: String,
       mainClass: String): String = {
     val sb = new StringBuffer
     for(line <- sbt.IO.readLinesURL(bashTemplateSource, charset)) {
       
       val fixed =
         line.replaceAll("@@APP_NAME@@", name).
              replaceAll("@@APP_MAIN_CLASS@@", mainClass).
              replaceAll("@@APP_ENV_NAME@@", makeEnvFriendlyName(name))
       
       sb append fixed
       sb append "\r\n"
     }
    sb.toString
  }
}