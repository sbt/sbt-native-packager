package com.typesafe.sbt.packager.archetypes

/**
 * Constructs a bash script for running a java application.
 * 
 * Makes use of the associated bash-template, with a few hooks
 * 
 */
object JavaAppBashScript {
  
  private[this] def bashTemplateSource =
    getClass.getResource("bash-template")
  private[this] def charset =
    java.nio.charset.Charset.forName("UTF-8")
    
  /** Creates the block of defines for a script.
   * 
   * @param mainClass The required "main" method class we use to run the program.
   * @param appClasspath A sequence of relative-locations (to the lib/ folder) of jars
   *                     to include on the classpath.
   * @param configFile An (optional) filename from which the script will read arguments.
   * @param extras  Any additional defines/commands that should be run in this script. 
   */
  def makeDefines(
      mainClass: String,
      appClasspath: Seq[String] = Seq("*"),
      configFile: Option[String] = None,
      extras: Seq[String] = Nil): Seq[String] =
       Seq(mainClassDefine(mainClass)) ++
       (configFile map configFileDefine).toSeq ++
       Seq(makeClasspathDefine(appClasspath)) ++
       extras
       
  private def makeClasspathDefine(cp: Seq[String]): String = {
    val fullString = cp map (n => "$lib_dir/"+n) mkString ":"
    "declare -r app_classpath=\""+fullString+"\"\n"
  }  
  def generateScript(defines: Seq[String]): String = {
     val sb = new StringBuffer
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