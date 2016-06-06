package com.typesafe.sbt
package packager
package windows

import Keys._
import sbt._

import collection.mutable.ArrayBuffer

case class WindowsProductInfo(
  id: String, // UUID of the package
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

sealed trait FeatureComponent
/** Define a new feature, that will be selectable in the default MSI. */
case class WindowsFeature(
  id: String,
  title: String,
  desc: String,
  absent: String = "allow",
  level: String = "1",
  display: String = "collapse",
  components: Seq[FeatureComponent] = Seq.empty
) extends FeatureComponent {}
/** Adds a file into a given windows feature. */
case class ComponentFile(
  source: String,
  editable: Boolean = false
) extends FeatureComponent
/** Define wix namespace definitions, that depend on the major version of Wix tools **/
case class NamespaceDefinitions(
  majorVersionNumber: Int,
  namespace: String,
  utilExtension: String
)
/**
 * Will add the directory to the windows path.  NOTE: Only one of these
 * per MSI.
 */
case class AddDirectoryToPath(dir: String = "") extends FeatureComponent
case class AddShortCuts(
  target: Seq[String],
  workingDir: String = "INSTALLDIR"
) extends FeatureComponent

// TODO - Shortcut as a component element.

/** Helper functions to deal with Wix/CAB craziness. */
object WixHelper {
  /** Generates a windows friendly GUID for use in random locations in the build. */
  def makeGUID: String = java.util.UUID.randomUUID.toString

  // TODO - Fragment out this function a bit so it's not so ugly/random.  
  def makeWixProductConfig(name: String, product: WindowsProductInfo, features: Seq[WindowsFeature], license: Option[File] = None): scala.xml.Node = {
    // TODO - First we find directories...
    // Adds all subdirectories... there was a bug when there were directories with only subdirs and no files in the tree,
    // so there was a gap and dirXml failed to create some directories
    def allParentDirs(f: File): Seq[File] = Option(f).toSeq.flatMap(f => f +: allParentDirs(f.getParentFile))
    val filenamesPrep =
      for {
        f <- features
        ComponentFile(name, _) <- f.components
      } yield allParentDirs(file(name))
    val filenames = filenamesPrep.flatten.map(_.toString.replaceAll("\\\\", "/")).filter(_ != "")
    // Now for directories...
    def parentDir(filename: String) = { filename take (filename lastIndexOf '/') }
    def simpleName(filename: String) = {
      val lastSlash =
        if (filename contains '/') filename lastIndexOf '/'
        else filename lastIndexOf '\\'
      filename drop (lastSlash + 1)
    }
    val dirs = (filenames map parentDir).distinct;
    // Now we need our directory tree xml?
    val dirToChildren = dirs groupBy parentDir;
    def dirXml(currentDir: String): scala.xml.Node = if (!currentDir.isEmpty) {
      val children = dirToChildren.getOrElse(currentDir, Seq.empty)
      <Directory Id={ cleanStringForId(currentDir) } Name={ simpleName(currentDir) }>
        {
          children map dirXml
        }
      </Directory>
    } else <!-- -->

    // We need component helpers...
    case class ComponentInfo(id: String, xml: scala.xml.Node)
    def makeComponentInfo(c: FeatureComponent): ComponentInfo = c match {
      case w: WindowsFeature => sys.error("Nested windows features currently unsupported!")
      case AddDirectoryToPath(dir) =>
        val dirRef = if (dir.isEmpty) "INSTALLDIR" else cleanStringForId(dir)
        val homeEnvVar = archetypes.JavaAppBatScript.makeEnvFriendlyName(name) + "_HOME"
        val pathAddition =
          if (dir.isEmpty) "%" + homeEnvVar + "%"
          else "[INSTALLDIR]" + dir.replaceAll("\\/", "\\\\")
        val id = cleanStringForId(dir).takeRight(65) + "PathC"
        val guid = makeGUID
        val xml =
          <DirectoryRef Id={ dirRef }>
            <Component Id={ id } Guid={ guid }>
              <CreateFolder/>
              <Environment Id={ homeEnvVar } Name={ homeEnvVar } Value="[INSTALLDIR]" Permanent="no" Action="set" System="yes"/>
              <Environment Id="PATH" Name="PATH" Value={ pathAddition } Permanent="no" Part="last" Action="set" System="yes"/>
            </Component>
          </DirectoryRef>
        ComponentInfo(id, xml)
      case ComponentFile(name, editable) =>
        val uname = name.replaceAll("\\\\", "/")
        val dir = parentDir(uname).replaceAll("//", "/").stripSuffix("/").stripSuffix("/")
        val dirRef = if (dir.isEmpty) "INSTALLDIR" else cleanStringForId(dir)
        val fname = simpleName(uname)
        val id = cleanStringForId(uname).takeRight(67) // Room for "fl_"
        val xml =
          <DirectoryRef Id={ dirRef }>
            <Component Id={ id } Guid={ makeGUID }>
              <File Id={ "fl_" + id } Name={ cleanFileName(fname) } DiskId='1' Source={ cleanFileName(uname) }>
                {
                  if (editable) {
                    <xml:group>
                      <util:PermissionEx User="Administrators" GenericAll="yes"/>
                      <util:PermissionEx User="Users" GenericAll="yes"/>
                    </xml:group>
                  } else Seq.empty
                }
              </File>
            </Component>
          </DirectoryRef>
        ComponentInfo(id, xml)
      // TODO - To have shortcuts, you MUST put something in the registry.  Here,
      // We should have shortcuts actually provide us with what they want in the registry,
      // rather than forcing it to be something.
      // Also, we need some mechanism to ensure the start menu folder is removed in the event
      // that we remove all menu items.
      case AddShortCuts(targets, workingDir) =>
        val targetSize = targets.size.toString.size
        val id = cleanStringForId("shortcut_" + makeGUID).takeRight(67 - targetSize) // Room for "_SC"+incremental number
        val xml =
          <DirectoryRef Id="ApplicationProgramsFolder">
            <Component Id={ id } Guid={ makeGUID }>
              {
                for ((target, i) <- targets.zipWithIndex) yield {
                  val name = simpleName(target)
                  val desc = "Edit configuration file: " + name
                  val cleanName = name.replaceAll("[\\.-\\\\//]+", "_")
                  <Shortcut Id={ id + "_SC" + (s"%0${targetSize}d").format(i + 1) } Name={ cleanName } Description={ desc } Target={ "[INSTALLDIR]\\" + target.replaceAll("\\/", "\\\\") } WorkingDirectory="INSTALLDIR"/>
                }
              }
              <RegistryValue Root="HKCU" Key={ "Software\\" + product.maintainer + "\\" + name } Name="installed" Type="integer" Value="1" KeyPath="yes"/>
            </Component>
          </DirectoryRef>
        ComponentInfo(id, xml)
    }

    val componentMap =
      (for (f <- features) yield {
        // TODO - we need to support more than "Component File".
        val componentInfos =
          f.components map makeComponentInfo
        f.id -> componentInfos
      }).toMap

    val removeId = cleanStringForId("ApplicationProgramsFolderRemove").takeRight(67)
    <xml:group>
      <!-- Define the directories we use -->
      <Directory Id='TARGETDIR' Name='SourceDir'>
        <Directory Id="ProgramMenuFolder">
          <Directory Id="ApplicationProgramsFolder" Name={ name }>
            <Component Id={ removeId }>
              <RemoveFolder Id="ApplicationProgramsFolderRemove" On="uninstall"/>
            </Component>
          </Directory>
        </Directory>
        <Directory Id='ProgramFilesFolder' Name='PFiles'>
          <Directory Id='INSTALLDIR' Name={ name }>
            { dirToChildren("") map dirXml }
          </Directory>
        </Directory>
      </Directory>
      <!-- Now define the components -->
      {
        for {
          (fid, components) <- componentMap
          ComponentInfo(cid, xml) <- components
        } yield xml
      }
      <!-- Now define the features! -->
      <Feature Id='Complete' Title={ product.title } Description={ product.description } Display='expand' Level='1' ConfigurableDirectory='INSTALLDIR'>
        {
          for (f <- features)
            yield <Feature Id={ f.id } Title={ f.title } Description={ f.desc } Level={ f.level } Absent={ f.absent }>
                    {
                      for (ComponentInfo(id, _) <- componentMap.getOrElse(f.id, Seq.empty))
                        yield <ComponentRef Id={ id }/>
                    }
                  </Feature>

        }
      </Feature>
      <MajorUpgrade AllowDowngrades="no" Schedule="afterInstallInitialize" DowngradeErrorMessage="A later version of [ProductName] is already installed. Setup will now exit."/>
      <UIRef Id="WixUI_FeatureTree"/>
      <UIRef Id="WixUI_ErrorProgressText"/>
      <Property Id="WIXUI_INSTALLDIR" Value="INSTALLDIR"/>
      {
        license.toSeq map { file =>
          <WixVariable Id="WixUILicenseRtf" Value={ file.getAbsolutePath }/>
        }
      }
    </xml:group>
  }

  def makeWixConfig(
    name: String, // package name
    product: WindowsProductInfo,
    namespaceDefinitions: NamespaceDefinitions,
    rest: xml.Node
  ): xml.Node = {
    <Wix xmlns={ namespaceDefinitions.namespace } xmlns:util={ namespaceDefinitions.utilExtension }>
      <Product Id={ product.id } Name={ product.title } Language='1033' Version={ product.version } Manufacturer={ product.maintainer } UpgradeCode={ product.upgradeId }>
        <Package Description={ product.description } Comments={ product.comments } Manufacturer={ product.maintainer } InstallScope={ product.installScope } InstallerVersion={ product.installerVersion } Compressed={ if (product.compressed) "yes" else "no" }/>
        <Media Id='1' Cabinet={ cleanStringForId(name.toLowerCase).takeRight(66) + ".cab" } EmbedCab='yes'/>
        { rest }
      </Product>
    </Wix>
  }

  /**
   * Wix namespace changed from major version 3 to 4.
   * TODO: Not sure if schema of 2006 is compatible with major versions < 3
   */
  def getNameSpaceDefinitions(majorVersion: Int): NamespaceDefinitions = {
    if (majorVersion <= 3)
      NamespaceDefinitions(
        majorVersion,
        namespace = "http://schemas.microsoft.com/wix/2006/wi",
        utilExtension = "http://schemas.microsoft.com/wix/UtilExtension"
      )
    else
      NamespaceDefinitions(
        majorVersion,
        namespace = "http://wixtoolset.org/schemas/v4/wxs",
        utilExtension = "http://wixtoolset.org/schemas/v4/wxs/util"
      )
  }

  /**
   * Modifies a string to be Wix ID friendly by removing all the bad
   * characters and replacing with _.  Also limits the width to 70 (rather than
   * 72) so we can safely add a few later.
   */
  def cleanStringForId(n: String) = {
    n.replaceAll("[^0-9a-zA-Z_]", "_").takeRight(60) + (math.abs(n.hashCode).toString + "xxxxxxxxx").substring(0, 9)
  }

  /** Cleans a file name for the Wix pre-processor.  Every $ should be doubled. */
  def cleanFileName(n: String) = {
    n.replaceAll("\\$", "\\$\\$").replaceAll("\\/", "\\\\")
  }
  /** Takes a file and generates an ID for it. */
  def makeIdFromFile(f: File) = cleanStringForId(f.getName)

  /**
   * Constructs a set of componentRefs and the directory/file WIX for
   * all files in a given directory.
   *
   * @return A tuple where the first item is all the Component Ids created,
   *         and the second is the Directory/File/Component XML.
   */
  @deprecated("Use higher level abstraction", "6/28/13")
  def generateComponentsAndDirectoryXml(dir: File, id_prefix: String = ""): (Seq[String], scala.xml.Node) = {
    def makeId(f: File) = cleanStringForId(IO.relativize(dir, f) map (id_prefix+) getOrElse (id_prefix + f.getName))
    def handleFile(f: File): (Seq[String], scala.xml.Node) = {
      val id = makeId(f)
      val xml = (
        <Component Id={ id } Guid='*'>
          <File Id={ cleanStringForId(id + "_file") } Name={ cleanFileName(f.getName) } DiskId='1' Source={ cleanFileName(f.getAbsolutePath) }/>
        </Component>
      )
      (Seq(id), xml)
    }
    def handleDirectory(dir: File): (Seq[String], scala.xml.Node) = {
      val buf: ArrayBuffer[String] = ArrayBuffer.empty
      val xml = (
        <Directory Id={ makeId(dir) } Name={ dir.getName }>
          {
            for {
              file <- IO.listFiles(dir)
              (ids, xml) = recursiveHelper(file)
            } yield {
              buf.appendAll(ids)
              xml
            }
          }
        </Directory>
      )
      (buf.toSeq, xml)
    }
    def recursiveHelper(f: File): (Seq[String], scala.xml.Node) =
      if (f.isDirectory) handleDirectory(f)
      else handleFile(f)

    recursiveHelper(dir)
  }
}
