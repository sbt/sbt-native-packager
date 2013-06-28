package com.typesafe.sbt
package packager
package windows

import Keys._
import sbt._

import collection.mutable.ArrayBuffer

case class WindowsProductInfo(
  id: String,  // UUID of the package
  title: String, // Human readable name of the package
  version: String, // Windows version
  maintainer: String,
  description: String,
  upgradeId: String, // UUID for upgrading
  comments: String = "",
  installScope: String = "perMachine",
  installerVersion: String = "200",
  compressed: Boolean = true
)

/** Helper functions to deal with Wix/CAB craziness. */
object WixHelper {
  /** Generates a windows friendly GUID for use in random locations in the build. */
  def makeGUID: String = java.util.UUID.randomUUID.toString
  
  
  def makeWixConfig(
      name: String, // package name
      product: WindowsProductInfo,
      rest: xml.Node): xml.Node = {
    <Wix xmlns='http://schemas.microsoft.com/wix/2006/wi' 
     xmlns:util='http://schemas.microsoft.com/wix/UtilExtension'>
      <Product Id={product.id} 
            Name={product.title} 
            Language='1033'
            Version={product.version}
            Manufacturer={product.maintainer} 
            UpgradeCode={product.upgradeId}>
        <Package Description={product.description}
              Comments={product.comments}
              Manufacturer={product.maintainer} 
              InstallScope={product.installScope}
              InstallerVersion={product.installerVersion}
              Compressed={if(product.compressed) "yes" else "no"} />
         <Media Id='1' Cabinet={name+".cab"} EmbedCab='yes' />
         {rest}
       </Product>
    </Wix>
  }
  
  /** Modifies a string to be Wix ID friendly by removing all the bad 
   * characters and replacing with _.  Also limits the width to 70 (rather than
   * 72) so we can safely add a few later.  We assume that's unique enough. 
   */
  def cleanStringForId(n: String) = n.replaceAll("[^0-9a-zA-Z_]", "_").takeRight(70)
  
  /** Cleans a file name for the Wix pre-processor.  Every $ should be doubled. */
  def cleanFileName(n: String) = n.replaceAll("\\$", "\\$\\$")
  /** Takes a file and generates an ID for it. */
  def makeIdFromFile(f: File) = cleanStringForId(f.getName)
  
  /** Constructs a set of componentRefs and the directory/file WIX for
   * all files in a given directory.
   *
   * @return A tuple where the first item is all the Component Ids created, 
   *         and the second is the Directory/File/Component XML.
   */
  def generateComponentsAndDirectoryXml(dir: File, id_prefix: String =""): (Seq[String], scala.xml.Node) = {
    def makeId(f: File) = cleanStringForId(IO.relativize(dir, f) map (id_prefix+) getOrElse (id_prefix+f.getName))
    def handleFile(f: File): (Seq[String], scala.xml.Node) = {
      val id = makeId(f)
      val xml = (
        <Component Id={id} Guid='*'>
          <File Id={cleanStringForId(id +"_file")} Name={cleanFileName(f.getName)} DiskId='1' Source={cleanFileName(f.getAbsolutePath)} />
        </Component>)
      (Seq(id), xml)
    }
    def handleDirectory(dir: File): (Seq[String], scala.xml.Node) = {
      val buf: ArrayBuffer[String] = ArrayBuffer.empty
      val xml = (
        <Directory Id={makeId(dir)} Name={dir.getName}>
        {  for {
            file <- IO.listFiles(dir)
            (ids, xml) = recursiveHelper(file)
           } yield {
             buf.appendAll(ids)
             xml
           }
        }
        </Directory>)
      (buf.toSeq, xml)
    }
    def recursiveHelper(f: File): (Seq[String], scala.xml.Node) =
      if(f.isDirectory) handleDirectory(f)
      else handleFile(f)
      
    recursiveHelper(dir)
  }
}
